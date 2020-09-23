package templating;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.text.SimpleDateFormat;
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
import templating.util.Rfc1342Directive;

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

	protected Generator                      parent;
	protected GeneratorConfig                generatorConfig;
	protected Properties                     templates;
	protected Map<String,Properties>         localization;
	protected Map<String,Map<String,String>> mergedLocalizations;
	protected Configuration                  freemarkerConfig;
	protected long                           lastModified;

	/**
	 * Constructor.
	 */
	public Generator(Generator parent, GeneratorConfig generatorConfig) {
		this.parent           = parent;
		this.generatorConfig  = generatorConfig;
		this.lastModified     = System.currentTimeMillis();

		// FreeMarker configuration is always specific to directory.
		freemarkerConfig = new Configuration(Configuration.VERSION_2_3_29);
		freemarkerConfig.setTemplateLoader(this);
		freemarkerConfig.setDefaultEncoding("UTF-8");
		freemarkerConfig.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
		freemarkerConfig.setLogTemplateExceptions(false);
		freemarkerConfig.setWrapUncheckedExceptions(true);
		freemarkerConfig.setFallbackOnNullLoopVariable(false);
		freemarkerConfig.setSharedVariable("quotedPrintable", new Rfc1342Directive(false));
		freemarkerConfig.setSharedVariable("qp", new Rfc1342Directive(false));
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

			// Do only when we are in sub-folder (if configured)
			if ((generatorConfig.templateMachineConfig.getSubDir() == null) || generatorConfig.templateMachineConfig.getSubDir().equals(generatorConfig.sourceDir) || FileUtils.directoryContains(generatorConfig.templateMachineConfig.getSubDir(), generatorConfig.sourceDir)) {
				// Process each file now with each language
				for (File child : generatorConfig.sourceDir.listFiles()) {
					if (!child.getName().startsWith("__") && isValidFile(child) && child.isFile() && child.canRead()) {
						// Now for each language
						Set<String> languages = getLanguages();
						if (languages.size() > 1) {
							for (String language : languages) {
								File outFile = new File(new File(generatorConfig.outputDir, language), child.getName());
								generateFile(child, language, outFile);
							}
						} else if (languages.size() > 0) {
							File outFile = new File(generatorConfig.outputDir, child.getName());
							generateFile(child, languages.iterator().next(), outFile);
						} else {
							File outFile = new File(generatorConfig.outputDir, child.getName());
							generateFile(child, "default", outFile);
						}
					}
				}
			}
		} catch (Throwable t) {
			throw new TemplatingException("Cannot generate files in "+generatorConfig.sourceDir.getPath(), t);
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
		Template temp = freemarkerConfig.getTemplate(templateFile.getName());
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
			rc.put("runDate", DATE_FORMATTER.format(generatorConfig.templateMachineConfig.getGenerationTime()));
			rc.put("runTime", TIME_FORMATTER.format(generatorConfig.templateMachineConfig.getGenerationTime()));

			mergedLocalizations.put(language, rc);
		}
		return rc;
	}

	/**
	 * Override parent definitions of templates.
	 * @throws IOException - when the templates cannot be read
	 */
	protected void loadLocalTemplates() throws IOException {
		File tDir = new File(generatorConfig.sourceDir, "__templates");
		if (tDir.exists() && tDir.isDirectory() && tDir.canRead()) {
			for (File child : tDir.listFiles()) {
				if (child.isFile() && child.canRead() && isValidFile(child)) {
					templates.setProperty(child.getName(), FileReadUtils.readFile(child, generatorConfig.templateMachineConfig.getReadEncoding()));
				} else if (child.isDirectory() && child.canRead()) {
					loadLocalSubTemplates(child.getName(), child);
				}
			}
		}
	}

	/**
	 * Load recursively templates in subfolders.
	 * @param namePrefix  - the prefix of the template name (name of folder)
	 * @param dir         - the folder to read
	 * @throws IOException - when the template cannot be read
	 */
	protected void loadLocalSubTemplates(String namePrefix, File dir) throws IOException {
		for (File child : dir.listFiles()) {
			if (child.isFile() && child.canRead() && isValidFile(child)) {
				templates.setProperty(namePrefix+"/"+child.getName(), FileReadUtils.readFile(child, generatorConfig.templateMachineConfig.getReadEncoding()));
			} else if (child.isDirectory()) {
				loadLocalSubTemplates(namePrefix+"/"+child.getName(), child);
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
		File tDir = new File(generatorConfig.sourceDir, "__localization");
		if (tDir.exists() && tDir.isDirectory() && tDir.canRead()) {
			for (File child : tDir.listFiles()) {
				if (child.isFile() && child.canRead() && isValidFile(child)) {
					String language = FilenameUtils.getBaseName(child.getName());
					Properties values = new Properties();
					values.load(FileReadUtils.getReader(child, generatorConfig.templateMachineConfig.getReadEncoding()));

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
			path = parent.getRelativePath(generatorConfig.sourceDir) + File.separator;
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
			File f = new File(generatorConfig.sourceDir, name);
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
			return FileReadUtils.getReader((File)templateSource, generatorConfig.templateMachineConfig.getReadEncoding());
		}
		return new StringReader(getTemplate(templateSource.toString()));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void closeTemplateSource(Object templateSource) throws IOException {
	}

	
	/**
	 * Returns true when a file (template or localization) can be used for templating.
	 * <p>This is being used for .bak, ~ or .swap files (temporary and backup files).</p>
	 * @param file - the file to be checked
	 * @return {@code true} when file can be used in template reading
	 */
	public boolean isValidFile(File file) {
		if (file.equals(generatorConfig.templateMachineConfig.configFile)) return false;
		String name = file.getName();
		if (name.startsWith(".")) return false;
		if (name.endsWith("~")) return false;
		if (name.endsWith(".bak")) return false;
		return true;
	}

}
