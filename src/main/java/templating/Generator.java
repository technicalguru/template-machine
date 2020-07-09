package templating;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import templating.util.FileReadUtils;

/**
 * Generates all files in a directory (not recursively!)
 * @author ralph
 *
 */
public class Generator implements Runnable, TemplateLoader {

	/** The logger */
	public static Logger log = LoggerFactory.getLogger(Generator.class);

	protected Generator parent;
	protected File      dir;
	protected File      outDir;

	protected Properties templates;
	protected Map<String,Properties> localization;
	protected Map<String,Map<String,String>> mergedLocalizations;
	protected Charset    readEncoding;
	protected Charset    writeEncoding;
	protected Configuration templateConfig;
	protected long       lastModified;

	/**
	 * Constructor.
	 */
	public Generator(Generator parent, File dir, File outDir) {
		this.parent        = parent;
		this.dir           = dir;
		this.outDir        = outDir;
		this.readEncoding  = Charset.forName("UTF-8");
		this.writeEncoding = Charset.forName("UTF-8");
		this.lastModified  = System.currentTimeMillis();

		// This is a bit weird. Template loader needs to be singleton as well as configuration. TODO
		templateConfig = new Configuration(Configuration.VERSION_2_3_29);
		templateConfig.setTemplateLoader(this);
		templateConfig.setDefaultEncoding("UTF-8");
		templateConfig.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
		templateConfig.setLogTemplateExceptions(false);
		templateConfig.setWrapUncheckedExceptions(true);
		templateConfig.setFallbackOnNullLoopVariable(false);
	}

	/**
	 * 
	 * {@inheritDoc}
	 */
	public void run() {
		try {
			// Prepare
			if (parent != null) {
				templates    = new Properties(parent.getTemplates());
			} else {
				templates    = new Properties();
			}
			loadLocalTemplates();
			loadLocalLocalization();

			// Process each file now with each language
			for (File child : dir.listFiles()) {
				if (!child.getName().startsWith("__") && Project.isValidFile(child) && child.isFile() && child.canRead()) {
					// Now for each language
					Set<String> languages = getLanguages();
					if (languages.size() > 1) {
						for (String language : languages) {
							File outFile = new File(new File(outDir, language), child.getName());
							generateFile(child, language, outFile);
						}
					} else if (languages.size() > 0) {
						File outFile = new File(outDir, child.getName());
						generateFile(child, languages.iterator().next(), outFile);
					} else {
						File outFile = new File(outDir, child.getName());
						generateFile(child, "default", outFile);
					}
				}
			}
		} catch (Throwable t) {
			throw new TemplatingException("Cannot generate files in "+dir.getPath(), t);
		}
	}

	/**
	 * Process a single template and generates the file
	 * @param templateFile - the template file
	 * @param language     - the language to be used (cannot be null)
	 * @param outFile      - the output file
	 */
	protected void generateFile(File templateFile, String language, File outFile) throws IOException, TemplateException {
		// Ignore when the is a language specific template file
		File langTemplate = new File(new File(templateFile.getParentFile(), language), templateFile.getName());
		if (langTemplate.exists()) return;
		
		// Ignore when the template file is already language specific
		String parentName = templateFile.getParentFile().getName();
		if (languageExists(parentName)) {
			// Only process when the language is the same
			if (!parentName.equals(language)) return;
			// But we need to change the output file
			outFile = new File(outFile.getParentFile().getParentFile(), outFile.getName());
		}
		
		log.info("Generating "+outFile.getPath()+"...");
		// Ensure the parent dir exists
		FileUtils.forceMkdirParent(outFile);
		Template temp = templateConfig.getTemplate(templateFile.getName());
		temp.process(getMergedLanguage(language), new FileWriter(outFile, this.writeEncoding));
	}

	/**
	 * Returns the templates.
	 * @return the templates
	 */
	public Properties getTemplates() {
		return templates;
	}

	/**
	 * Returns the localizations
	 * @return the localizations
	 */
	public Map<String,Properties> getLocalization() {
		return localization;
	}

	protected Map<String,String> getMergedLanguage(String language) {
		Map<String,String> rc = mergedLocalizations.get(language);
		if (rc == null) {
			rc = new HashMap<>();

			Properties defaults = localization.get("default");
			if (defaults != null) {
				for (Map.Entry<Object, Object> entry : defaults.entrySet()) {
					rc.put((String)entry.getKey(), (String)entry.getValue());
				}
			}
			Properties values = localization.get(language);
			if (values != null) {
				for (Map.Entry<Object, Object> entry : values.entrySet()) {
					rc.put((String)entry.getKey(), (String)entry.getValue());
				}
			}
			mergedLocalizations.put(language, rc);
		}
		return rc;
	}

	/**
	 * Override parent definitions of templates.
	 * @throws IOException when the templates cannot be read
	 */
	protected void loadLocalTemplates() throws IOException {
		File tDir = new File(dir, "__templates");
		if (tDir.exists() && tDir.isDirectory() && tDir.canRead()) {
			for (File child : tDir.listFiles()) {
				if (child.isFile() && child.canRead() && Project.isValidFile(child)) {
					templates.setProperty(child.getName(), FileReadUtils.readFile(child, readEncoding));
				}
			}
		}
	}

	/**
	 * Override parent localization values.
	 * @throws IOException the the localization files cannot be read
	 */
	protected void loadLocalLocalization() throws IOException {
		// Get a copy of all parent languages
		localization = new HashMap<>();
		mergedLocalizations = new HashMap<>();

		if (parent != null) {
			for (Map.Entry<String,Properties> entry : parent.getLocalization().entrySet()) {
				Properties copy = new Properties();
				for (Map.Entry<Object,Object> l : entry.getValue().entrySet()) {
					copy.put(l.getKey(), l.getValue());
				}
				localization.put(entry.getKey(), copy);
			};
		}

		// Load the local overrides
		File tDir = new File(dir, "__localization");
		if (tDir.exists() && tDir.isDirectory() && tDir.canRead()) {
			for (File child : tDir.listFiles()) {
				if (child.isFile() && child.canRead() && Project.isValidFile(child)) {
					String language = FilenameUtils.getBaseName(child.getName());
					Properties values = new Properties();
					values.load(FileReadUtils.getReader(child, this.readEncoding));

					// Add overrides to existing
					Properties my = localization.get(language);
					if (my == null) {
						my = new Properties();
						localization.put(language, my);
					}
					for (Map.Entry<Object,Object> entry : values.entrySet()) {
						my.put(entry.getKey(), entry.getValue());
					}
				}
			}
		}
	}

	protected boolean languageExists(String lang) {
		// Default never exists
		if ("default".equals(lang)) return false;
		return localization.containsKey(lang);
	}
	
	/**
	 * Returns all existing languages.
	 * @return set of language keys
	 */
	public Set<String> getLanguages() {
		Set<String> rc = new HashSet<>(localization.keySet());
		if (parent != null) {
			for (String language : parent.getLanguages()) {
				rc.add(language);
			}
		}
		// Never return the default one
		rc.remove("default");
		return rc;
	}

	/**
	 * Returns the template with given name.
	 * @param name - name of template (may be from parents)
	 * @return the template if exists, {@code null} otherwise
	 */
	public String getTemplate(String name) {
		return templates.getProperty(name);
	}

	/**
	 * Returns the localized value of the given key 
	 * @param language - the language to be used
	 * @param key      - the key of the value
	 * @return the localized value or {@code ""} (empty string) of not found
	 */
	public String getLocalizedString(String language, String key) {
		Properties locals = localization.get(language);
		if (locals == null) {
			return "";
		}
		String rc = locals.getProperty(key);
		if (rc == null) {
			rc = "";
		}
		return rc;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object findTemplateSource(String name) throws IOException {
		if (templates.getProperty(name) == null) {
			// Exception: the template is the local file
			File f = new File(dir, name);
			if (f.exists() && f.isFile() && f.canRead()) {
				return f;
			}
			// log.error("Cannot find "+name+" ("+f.getAbsolutePath()+")");
			return null;
		}
		return name;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getLastModified(Object templateSource) {
		return lastModified;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Reader getReader(Object templateSource, String encoding) throws IOException {
		if (templateSource instanceof File) {
			return FileReadUtils.getReader((File)templateSource, readEncoding);
		}
		return new StringReader(getTemplate(templateSource.toString()));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void closeTemplateSource(Object templateSource) throws IOException {
	}


}
