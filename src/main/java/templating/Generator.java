package templating;

import java.io.File;
import java.io.FileReader;
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
	protected Charset    encoding;
	protected Configuration templateConfig;
	protected long       lastModified;
	
	/**
	 * Constructor.
	 */
	public Generator(Generator parent, File dir, File outDir) {
		this.parent       = parent;
		this.dir          = dir;
		this.outDir       = outDir;
		this.encoding     = Charset.defaultCharset();
		this.lastModified = System.currentTimeMillis();
		
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
				localization = new HashMap<>(parent.getLocalization());
			} else {
				templates    = new Properties();
				localization = new HashMap<>();
			}
			loadLocalTemplates();
			loadLocalLocalization();

			// Process each file now with each language
			for (File child : dir.listFiles()) {
				if (!child.getName().startsWith("__") && !child.getName().startsWith(".") && !child.getName().endsWith("~") && child.isFile() && child.canRead()) {
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
		log.info("Generating "+outFile.getPath()+"...");
		// Ensure the parent dir exists
		FileUtils.forceMkdirParent(outFile);
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
	 * Returns the localizations
	 * @return the localizations
	 */
	public Map<String,Properties> getLocalization() {
		return localization;
	}

	/**
	 * Override parent definitions of templates.
	 * @throws IOException when the templates cannot be read
	 */
	protected void loadLocalTemplates() throws IOException {
		File tDir = new File(dir, "__templates");
		if (tDir.exists() && tDir.isDirectory() && tDir.canRead()) {
			for (File child : tDir.listFiles()) {
				if (child.isFile() && child.canRead() && !child.getName().startsWith(".") && !child.getName().endsWith("~")) {
					templates.setProperty(child.getName(), FileUtils.readFileToString(child, encoding));
				}
			}
		}
	}

	/**
	 * Override parent localization values.
	 * @throws IOException the the localization files cannot be read
	 */
	protected void loadLocalLocalization() throws IOException {
		// TODO Build a combination of all localizations (take the parent ones and add local ones!)
		File tDir = new File(dir, "__localization");
		if (tDir.exists() && tDir.isDirectory() && tDir.canRead()) {
			for (File child : tDir.listFiles()) {
				if (child.isFile() && child.canRead() && !child.getName().startsWith(".") && !child.getName().endsWith("~")) {
					String language = FilenameUtils.getBaseName(child.getName());
					Properties values = new Properties();
					values.load(new FileReader(child));
					localization.put(language, values);
				}
			}
		}
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
			if (parent != null) {
				return parent.getLocalizedString(language, key);
			}
			return "";
		}
		String rc = locals.getProperty(key);
		if (rc == null) {
			if (parent != null) {
				return parent.getLocalizedString(language, key);
			}
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
			log.error("Cannot find "+name+" ("+f.getAbsolutePath()+")");
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
			return new FileReader(templateSource.toString());
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
