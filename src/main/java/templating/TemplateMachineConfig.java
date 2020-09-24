/**
 * 
 */
package templating;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.Enumeration;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

/**
 * Describes configuration for a run.
 * @author ralph
 *
 */
public class TemplateMachineConfig {

	protected static Properties defaultConfig;

	protected File       projectRoot;
	protected File       outRoot;
	protected File       subDir;
	protected File       configFile;
	protected Properties config;
	protected Date       generationTime;
	protected Charset    readEncoding   = Charset.defaultCharset();
	protected Charset    writeEncoding  = Charset.defaultCharset();


	/**
	 * Constructor.
	 */
	public TemplateMachineConfig(File projectRoot, File outRoot, File configFile, Date generationTime) throws IOException {
		this(projectRoot, outRoot, loadConfigFile(configFile), generationTime);
		this.configFile = configFile;
	}

	/**
	 * Constructor.
	 */
	public TemplateMachineConfig(File projectRoot, File outRoot, Properties config, Date generationTime) {
		this.projectRoot    = projectRoot;
		this.outRoot        = outRoot;
		this.configFile     = null;
		this.config         = createFullConfig(config);
		this.generationTime = generationTime;
	}

	/**
	 * Create a properties object with the default keys underneath.
	 * @param config - the config properties
	 * @return the config with defaults
	 */
	private Properties createFullConfig(Properties config) {
		Properties rc = new Properties(getDefaultConfig());
		for (Object key : config.keySet()) {
			rc.setProperty((String)key, config.getProperty((String)key));
		}
		return rc;
	}

	/**
	 * Returns the encoding for reading templates and language files.
	 * @return the readEncoding
	 */
	public Charset getReadEncoding() {
		return readEncoding;
	}

	/**
	 * Sets the encoding for reading templates and language files.
	 * @param readEncoding - the readEncoding to set
	 */
	public void setReadEncoding(Charset readEncoding) {
		this.readEncoding = readEncoding;
	}

	/**
	 * Returns the encoding for writing generated files.
	 * @return the writeEncoding
	 */
	public Charset getWriteEncoding() {
		return writeEncoding;
	}

	/**
	 * Sets the encoding for writing generated files.
	 * @param writeEncoding - the writeEncoding to set
	 */
	public void setWriteEncoding(Charset writeEncoding) {
		this.writeEncoding = writeEncoding;
	}

	/**
	 * Returns the root director of the project to be generated.
	 * @return the projectRoot
	 */
	public File getProjectRoot() {
		return projectRoot;
	}

	/**
	 * Sets the root director of the project to be generated.
	 * @param projectRoot - the projectRoot to set
	 */
	public void setProjectRoot(File projectRoot) {
		this.projectRoot = projectRoot;
	}

	/**
	 * Returns the output root directory for this project.
	 * @return the outRoot
	 */
	public File getOutRoot() {
		return outRoot;
	}

	/**
	 * Sets the output root directory for this project.
	 * @param outRoot - the outRoot to set
	 */
	public void setOutRoot(File outRoot) {
		this.outRoot = outRoot;
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
	 * Returns the sub directory to be generated only (can be {@code null}).
	 * @return the subDir
	 */
	public File getSubDir() {
		return subDir;
	}

	/**
	 * Sets the sub directory to be generated only (can be {@code null}).
	 * @param subDir - the subDir to set
	 */
	public void setSubDir(File subDir) {
		this.subDir = subDir;
	}

	/**
	 * Returns the project config.
	 * @return the project config
	 */
	public Properties getConfig() {
		return config;
	}

	/**
	 * Sets the project config.
	 * @param config the project config to set
	 */
	public void setConfig(Properties config) {
		this.config = config;
	}

	/**
	 * Returns the value in the project config.
	 * @param key - config key
	 * @return the value or NULL
	 * @see java.util.Properties#getProperty(java.lang.String)
	 */
	public String getConfig(String key) {
		return config.getProperty(key);
	}

	/**
	 * Returns the value in the project config.
	 * @param key          - config key
	 * @param defaultValue - default value
	 * @return the value or the default value
	 * @see java.util.Properties#getProperty(java.lang.String, java.lang.String)
	 */
	public String getConfig(String key, String defaultValue) {
		return config.getProperty(key, defaultValue);
	}

	/**
	 * Returns all defined config keys.
	 * @return enumeration of config keys
	 * @see java.util.Properties#keys()
	 */
	public Enumeration<Object> getConfigKeys() {
		return config.keys();
	}

	/**
	 * Checks whether a certain key exists in config.
	 * @param key - the key to be checked
	 * @return {@code true} when config key exists, {@code false} otherwise
	 * @see java.util.Properties#containsKey(java.lang.Object)
	 */
	public boolean containsConfigKey(Object key) {
		return config.containsKey(key);
	}

	/**
	 * Returns all defined config keys as a set.
	 * @return all config keys
	 * @see java.util.Properties#keySet()
	 */
	public Set<Object> getConfigKeySet() {
		return config.keySet();
	}

	/**
	 * Returns all config keys and values as {@link Entry}.
	 * @return alss config keys and values
	 * @see java.util.Properties#entrySet()
	 */
	public Set<Entry<Object, Object>> getConfigEntrySet() {
		return config.entrySet();
	}

	/**
	 * Tests whether the file is any of the special files or directories in the config
	 * @param file - the file to test
	 * @return {@code true} when the file has a special meaning, {@code false} otherwise
	 */
	public boolean isSpecialFile(File file) {
		if (file.getName().equals(getConfig("templateDir"))) return true;	
		if (file.getName().equals(getConfig("localizationDir"))) return true;
		if (file.equals(configFile)) return true;
		return false;
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
	 * Load a configuration file.
	 * @param file - the file to be loaded
	 * @return the configuration
	 * @throws IOException when the file cannot be read
	 */
	public static Properties loadConfigFile(File file) throws IOException {
		Properties rc = new Properties();
		if (file.exists()) {
			if (!file.canRead()) {
				throw new TemplatingException("Cannot read "+file.getCanonicalPath());
			}
			rc.load(new FileReader(file));
		}
		return rc;
	}
}
