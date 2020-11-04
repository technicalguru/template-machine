package templating;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import templating.util.FileReadUtils;

/**
 * Holds all information and configuration in a directory.
 * @author ralph
 *
 */
public class Context {

	/** Default configuration values (required in root context only) */
	protected static Properties       defaultConfig;
	private   static SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("dd/MM/yyyy");
	private   static SimpleDateFormat TIME_FORMATTER = new SimpleDateFormat("HH:mm:ss");


	// Root information
	/** The source root directory where templates are located */
	private File    sourceRoot;
	/** the output root directory where files are generated */ 
	private File    outputRoot;
	/** the directory that is supposed to be generated only, usually equals {@link #sourceRoot} unless otherwise specified */
	private File    subRoot;
	/** The time of generation */
	private Date    generationTime;

	// Direct context information
	private Context                parent;
	private File                   sourceDir;
	private File                   outputDir;
	private Charset                readEncoding;
	private Charset                writeEncoding;
	private Properties             config;
	private Set<String>            languages;
	private Map<String,Properties> localizations;
	private Map<String,Map<String,String>> mergedLocalizations;
	private Properties             templates;
	private Set<File>              ignoredFiles;
	
	/**
	 * Root Constructor.
	 * @param sourceDir the source directory to be processed
	 * @param outputDir the output directory where to write to
	 * @param subRoot   the sub directory that shall be generated only
	 * @param config    the main configuration to base all values on
	 */
	public Context(File sourceDir, File outputDir, File subRoot, Properties config) {
		this.parent    = null;
		this.sourceDir = sourceDir;
		this.outputDir = outputDir;
		this.subRoot   = subRoot;
		this.config    = config;
		initContext();
	}

	/**
	 * Constructor for sub directories.
	 * @param parent    the parent context (is {@code null} in root context)
	 * @param sourceDir the source directory to be processed
	 * @param outputDir the output directory where to write to
	 */
	public Context(Context parent, File sourceDir, File outputDir) {
		this.parent    = parent;
		this.sourceDir = sourceDir;
		this.outputDir = outputDir;
		initContext();
	}

	/**
	 * Initializes the context by reading directory specific
	 * configuration and localization information.
	 */
	protected void initContext() {
		if (parent != null) {
			sourceRoot     = parent.sourceRoot;
			outputRoot     = parent.outputRoot;
			subRoot        = parent.subRoot;
			readEncoding   = parent.readEncoding;
			writeEncoding  = parent.writeEncoding;
			config         = parent.config;
			generationTime = parent.getGenerationTime();
			templates      = new Properties(parent.getTemplates());
			ignoredFiles   = new HashSet<>(parent.getIgnoredFiles());
		} else {
			sourceRoot     = sourceDir;
			outputRoot     = outputDir;
			readEncoding   = Charset.defaultCharset();
			writeEncoding  = Charset.defaultCharset();
			generationTime = new Date();
			templates      = new Properties();
			ignoredFiles   = new HashSet<>();
		}

		// Now load config for local context from .config file
		loadLocalConfig();

		// Load localizations
		loadLocalizations();

		// Load local templates
		loadLocalTemplates();

	}

	/**
	 * Loads the local configuration and parent configuration
	 * as well as local .config files in current directory {@link #sourceDir}.
	 */
	protected void loadLocalConfig() {
		try {
			File localConfigFile = new File(sourceDir, ".config");
			if (localConfigFile.exists() && localConfigFile.canRead()) {
				Properties p = new Properties(this.config);
				p.load(new FileReader(localConfigFile, readEncoding));
				this.config = p;
			}
		} catch (Throwable t) {
			throw new TemplatingException("Cannot load local config", t);
		}
	}

	/**
	 * Returns the configuration value of given key.
	 * @param key the key of the value
	 * @return the value valid for this context
	 */
	public String getConfigString(String key) {
		return config.getProperty(key, getDefaultConfig().getProperty(key));
	}

	/**
	 * Computes the required local languages and loads them.
	 */
	protected void loadLocalizations() {
		if (this.localizations == null) {
			this.localizations       = new HashMap<>();
			this.mergedLocalizations = new HashMap<>();
			this.languages           = new HashSet<>();
			String languageDefs[]    = getConfigString("languages").split(",");
			if ((languageDefs.length == 1) && languageDefs[0].equalsIgnoreCase("auto")) {
				loadAutoLanguages();
			} else {
				// Load always default
				loadLanguage("default", "default");

				boolean other = false;
				for (String language : languageDefs) {
					if (language.equals("other")) other = true;
					else {
						if (language.indexOf('=') > 0) {
							String langDef[] = language.split("=");
							if (langDef.length > 2) throw new TemplatingException("Cannot process language definition: "+language);
							loadLanguage(langDef[0], langDef[1]);
							this.languages.add(langDef[0]);
						} else {
							loadLanguage(language, language);
							this.languages.add(language);
						}
					}
				}
				if (other) {
					loadAutoLanguages();
				}

			}
		}
	}

	/**
	 * Loads a specific language
	 * @param key     the key this language will be available at
	 * @param loadKey the key that this language is available on file 
	 */
	protected void loadLanguage(String key, String loadKey) {
		try {
			Properties values       = new Properties();
			if (parent != null) {
				// Get the language from parent (stored under key)
				Properties parentValues = parent.getLocalizations(key);
				if (parentValues != null) values.putAll(parentValues);
				// Get the language from parent (stored under loadKey)
				parentValues = parent.getLocalizations(loadKey);
				if (parentValues != null) values.putAll(parentValues);
			}

			File lFile = new File(new File(sourceDir, getConfigString("localizationDir")), loadKey+".properties");
			if (lFile.isFile() && lFile.canRead()) {
				values.load(new FileReader(lFile, readEncoding));
			}

			localizations.put(key, values);
		} catch (Throwable t) {
			throw new TemplatingException("Cannot load language \""+key+"\" from key +\""+loadKey+"\"", t);
		}
	}

	/**
	 * Loads all languages available.
	 */
	protected void loadAutoLanguages() {
		// Which languages are available?
		// We first look at the parent
		if (parent != null) {
			for (String language : parent.getLanguages()) {
				loadLanguage(language, language);
				languages.add(language);
			}
		}

		// Now add each language that is available in local folder
		File lDir = new File(sourceDir, getConfigString("localizationDir"));
		if (lDir.exists() && lDir.isDirectory() && lDir.canRead()) {
			for (File child : lDir.listFiles()) {
				if (child.isFile() && child.canRead() && isValidFile(child)) {
					String language = FilenameUtils.getBaseName(child.getName());
					loadLanguage(language, language);
					if (!language.equalsIgnoreCase("default")) languages.add(language);
				}
			}
		}

	}

	/**
	 * Returns all languages in this context.
	 * @return the languages available
	 */
	public Collection<String> getLanguages() {
		return languages;
	}

	/**
	 * Returns whether language exists.
	 * @param lang - language key
	 * @return {@code true} when language exists ("default" key always returns false)
	 */
	public boolean hasLanguage(String lang) {
		// Default never exists
		if ("default".equals(lang)) return false;
		return localizations.containsKey(lang);
	}

	/**
	 * Returns the localization values for the given key.
	 * <p>Returns general language localization when country-specific localization cannot be found. However, default localization values are not returned.</p>
	 * @param key key of language (can be e.g. {@code de-de} or {@code de})
	 * @return the localization, country-specific or general or {@code null}. 
	 */
	protected Properties getLocalizations(String key) {
		Properties rc = localizations.get(key);
		if ((rc == null) && (key.indexOf('-') == 2)) {
			rc = localizations.get(key.substring(0, 2));
		}
		return rc;
	}

	/**
	 * Merge all language keys so all values are available for a specific language.
	 * @param language - the language key
	 * @return all keys including from default language
	 */
	protected Map<String,String> getMergedLocalization(String language) {
		Map<String,String> rc = mergedLocalizations.get(language);
		if (rc == null) {
			rc = new HashMap<>();

			Properties defaults = localizations.get("default");
			if (defaults != null) {
				for (Map.Entry<Object, Object> entry : defaults.entrySet()) {
					rc.put((String)entry.getKey(), (String)entry.getValue());
				}
			}
			Properties values = localizations.get(language);
			if (values != null) {
				for (Map.Entry<Object, Object> entry : values.entrySet()) {
					rc.put((String)entry.getKey(), (String)entry.getValue());
				}
			}

			// Set default values
			rc.put("languageKey", language);
			rc.put("runDate", DATE_FORMATTER.format(getGenerationTime()));
			rc.put("runTime", TIME_FORMATTER.format(getGenerationTime()));

			mergedLocalizations.put(language, rc);
		}
		return rc;
	}


	/**
	 * Override parent definitions of templates.
	 * @throws IOException - when the templates cannot be read
	 */
	protected void loadLocalTemplates() {
		try {
			File tDir = new File(sourceDir, getConfigString("templateDir"));
			if (tDir.exists() && tDir.isDirectory() && tDir.canRead()) {
				for (File child : tDir.listFiles()) {
					if (child.isFile() && child.canRead() && isValidFile(child)) {
						templates.setProperty(child.getName(), FileReadUtils.readFile(child, readEncoding));
					} else if (child.isDirectory() && child.canRead()) {
						loadLocalSubTemplates(child.getName(), child);
					}
				}
			}
		} catch (Throwable t) {
			throw new TemplatingException("Cannot load local templates", t);
		}
	}

	/**
	 * Load recursively templates in sub folders.
	 * @param namePrefix   the prefix of the template name (name of folder)
	 * @param dir          the folder to read
	 * @throws IOException when the template cannot be read
	 */
	protected void loadLocalSubTemplates(String namePrefix, File dir) throws IOException {
		for (File child : dir.listFiles()) {
			if (child.isFile() && child.canRead() && isValidFile(child)) {
				templates.setProperty(namePrefix+"/"+child.getName(), FileReadUtils.readFile(child, readEncoding));
			} else if (child.isDirectory()) {
				loadLocalSubTemplates(namePrefix+"/"+child.getName(), child);
			}
		}
	}

	/**
	 * Returns the templates.
	 * @return the templates
	 */
	public Properties getTemplates() {
		return templates;
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
	 * Returns the generation time for this run.
	 * @return the generationTime
	 */
	public Date getGenerationTime() {
		return generationTime;
	}

	/**
	 * Sets the generation time for this run.
	 * @param generationTime - the generationTime to set
	 */
	public void setGenerationTime(Date generationTime) {
		this.generationTime = generationTime;
	}

	/**
	 * Returns the sourceRoot.
	 * @return the sourceRoot
	 */
	public File getSourceRoot() {
		return sourceRoot;
	}

	/**
	 * Sets the sourceRoot.
	 * @param sourceRoot - the sourceRoot to set
	 */
	public void setSourceRoot(File sourceRoot) {
		this.sourceRoot = sourceRoot;
	}

	/**
	 * Returns the outputRoot.
	 * @return the outputRoot
	 */
	public File getOutputRoot() {
		return outputRoot;
	}

	/**
	 * Sets the outputRoot.
	 * @param outputRoot - the outputRoot to set
	 */
	public void setOutputRoot(File outputRoot) {
		this.outputRoot = outputRoot;
	}

	/**
	 * Returns the subRoot.
	 * @return the subRoot
	 */
	public File getSubRoot() {
		return subRoot;
	}

	/**
	 * Sets the subRoot.
	 * @param subRoot - the subRoot to set
	 */
	public void setSubRoot(File subRoot) {
		this.subRoot = subRoot;
	}

	/**
	 * Returns the parent.
	 * @return the parent
	 */
	public Context getParent() {
		return parent;
	}

	/**
	 * Sets the parent.
	 * @param parent - the parent to set
	 */
	public void setParent(Context parent) {
		this.parent = parent;
	}

	/**
	 * Returns the sourceDir.
	 * @return the sourceDir
	 */
	public File getSourceDir() {
		return sourceDir;
	}

	/**
	 * Sets the sourceDir.
	 * @param sourceDir - the sourceDir to set
	 */
	public void setSourceDir(File sourceDir) {
		this.sourceDir = sourceDir;
	}

	/**
	 * Returns the outputDir.
	 * @return the outputDir
	 */
	public File getOutputDir() {
		return outputDir;
	}

	/**
	 * Sets the outputDir.
	 * @param outputDir - the outputDir to set
	 */
	public void setOutputDir(File outputDir) {
		this.outputDir = outputDir;
	}

	/**
	 * Returns the readEncoding.
	 * @return the readEncoding
	 */
	public Charset getReadEncoding() {
		return readEncoding;
	}

	/**
	 * Sets the readEncoding.
	 * @param readEncoding - the readEncoding to set
	 */
	public void setReadEncoding(Charset readEncoding) {
		this.readEncoding = readEncoding;
	}

	/**
	 * Returns the writeEncoding.
	 * @return the writeEncoding
	 */
	public Charset getWriteEncoding() {
		return writeEncoding;
	}

	/**
	 * Sets the writeEncoding.
	 * @param writeEncoding - the writeEncoding to set
	 */
	public void setWriteEncoding(Charset writeEncoding) {
		this.writeEncoding = writeEncoding;
	}

	/**
	 * Returns whether the current sub directory is marked for generation.
	 * <p>Either {@link #subRoot} is empty or {@link #sourceDir} belongs to {@link #subRoot}.</p>
	 * @return {@code true} when the directory is safe to be generated
	 */
	public boolean canGenerateDirectory() {
		try {
			return (subRoot == null) || subRoot.equals(sourceDir) || FileUtils.directoryContains(subRoot, sourceDir);
		} catch (Throwable t) {
			throw new TemplatingException("Cannot detect whether subDir belongs to subRoot", t);
		}
	}

	/**
	 * Returns the default configuration.
	 * @return the configuration defaults
	 */
	public static Properties getDefaultConfig() {
		if (defaultConfig == null) {
			defaultConfig = new Properties();
			defaultConfig.setProperty("languages",       "auto");
			defaultConfig.setProperty("localizationDir", "__localization");
			defaultConfig.setProperty("templateDir",     "__templates");
		}
		return defaultConfig;
	}

	/**
	 * Sets the ignoredFiles.
	 * @param ignoredFiles - the ignoredFiles to set
	 */
	public void setIgnoredFiles(Set<File> ignoredFiles) {
		this.ignoredFiles = ignoredFiles;
	}

	/**
	 * Returns the ignoredFiles.
	 * @return the ignoredFiles
	 */
	public Set<File> getIgnoredFiles() {
		return ignoredFiles;
	}

	/**
	 * Adds file to list of ignore files.
	 * @param file file to be ignored
	 */
	public void ignoreFile(File file) {
		ignoredFiles.add(file);
	}
	
	/**
	 * Returns true when a file (template or localization) can be used for templating.
	 * <p>This is being used to ignore .bak, ~ or .swap files (temporary and backup files).</p>
	 * @param file - the file to be checked
	 * @return {@code true} when file can be used in template reading
	 */
	public boolean isValidFile(File file) {
		if (ignoredFiles.contains(file)) return false;
		String name = file.getName();
		if (name.startsWith(".")) return false;
		if (name.endsWith("~")) return false;
		if (name.endsWith(".bak")) return false;
		return true;
	}

	/**
	 * Tests whether the file is any of the special files or directories in the config
	 * @param file - the file to test
	 * @return {@code true} when the file has a special meaning, {@code false} otherwise
	 */
	public boolean isSpecialFile(File file) {
		if (file.getName().equals(getConfigString("templateDir"))) return true;	
		if (file.getName().equals(getConfigString("localizationDir"))) return true;
		if (ignoredFiles.contains(file)) return true;
		return false;
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
			path = parent.getRelativePath(sourceDir) + File.separator;
		}
		return path + file.getName();
	}

}
