package templating;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
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

	private static SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("dd/MM/yyyy");
	private static SimpleDateFormat TIME_FORMATTER = new SimpleDateFormat("HH:mm:ss");

	protected Generator  parent;
	protected File       dir;
	protected File       outDir;
	protected Date       generationTime;
	protected Charset    readEncoding;
	protected Charset    writeEncoding;

	protected Properties                     templates;
	protected Map<String,Properties>         localization;
	protected Map<String,Map<String,String>> mergedLocalizations;
	protected Configuration                  templateConfig;
	protected long                           lastModified;

	/**
	 * Constructor.
	 */
	public Generator(Generator parent, File dir, File outDir, Charset readEncoding, Charset writeEncoding, Date generationTime) {
		this.parent         = parent;
		this.dir            = dir;
		this.outDir         = outDir;
		this.readEncoding   = readEncoding;
		this.writeEncoding  = writeEncoding;
		this.generationTime = generationTime;
		this.lastModified   = System.currentTimeMillis();

		// FreeMarker configuration is always specific to directory.
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
	 * Process a single template and generates the file.
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
		
		// Prepare localization
		Map<String,String> localization = getMergedLanguage(language);
		localization.put("templateAbsPath", templateFile.getCanonicalPath());
		localization.put("templateRelPath", getRelativePath(templateFile));

		// Generate
		Template temp = templateConfig.getTemplate(templateFile.getName());
		temp.process(localization, new FileWriter(outFile));
	}

	/**
	 * Returns the templates.
	 * @return the templates
	 */
	public Properties getTemplates() {
		return templates;
	}

	/**
	 * Returns the localizations.
	 * @return the localizations
	 */
	public Map<String,Properties> getLocalization() {
		return localization;
	}

	/**
	 * Merge all language keys so all values are available for a specific language.
	 * @param language - the language key
	 * @return all keys including from default language
	 */
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
			
			// Set default values
			rc.put("languageKey", language);
			rc.put("runDate", DATE_FORMATTER.format(generationTime));
			rc.put("runTime", TIME_FORMATTER.format(generationTime));

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
		localization = new HashMap<>();
		mergedLocalizations = new HashMap<>();
		
		// Get a copy of all parent languages
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

	/**
	 * Returns whether language exists.
	 * @param lang - language key
	 * @return {@code true} when language exists ("default" key always returns false)
	 */
	protected boolean languageExists(String lang) {
		// Default never exists
		if ("default".equals(lang)) return false;
		return localization.containsKey(lang);
	}
	
	/**
	 * Returns all existing languages except "default".
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
	 * Returns the relative path  of the file in the project
	 * @param file - the file to relate
	 * @return the relative path
	 * @throws IOException - when an exception occurs
	 */
	protected String getRelativePath(File file) throws IOException {
		String path = "";
		if (parent != null) {
			path = parent.getRelativePath(dir) + File.separator;
		}
		return path + file.getName();
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
