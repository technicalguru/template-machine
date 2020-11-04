/**
 * 
 */
package templating;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * Describes configuration for a run.
 * @author ralph
 *
 */
public class TemplateMachineConfig {

	protected File       sourceDir;
	protected File       outputDir;
	protected File       subDir;
	protected File       configFile;
	protected Properties config;
	protected Date       generationTime;
	protected Charset    readEncoding   = Charset.defaultCharset();
	protected Charset    writeEncoding  = Charset.defaultCharset();
	private Set<File>    ignoredFiles   = new HashSet<>();


	/**
	 * Constructor.
	 */
	public TemplateMachineConfig(File projectRoot, File outputDir, Properties config, Date generationTime) {
		this.sourceDir      = projectRoot;
		this.outputDir      = outputDir;
		this.configFile     = null;
		this.config         = config;
		this.generationTime = generationTime;
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
	 * @return the sourceDir
	 */
	public File getSourceDir() {
		return sourceDir;
	}

	/**
	 * Sets the root director of the project to be generated.
	 * @param sourceDir - the sourceDir to set
	 */
	public void setSourceRoot(File sourceDir) {
		this.sourceDir = sourceDir;
	}

	/**
	 * Returns the output directory for this project.
	 * @return the outputDir
	 */
	public File getOutputDir() {
		return outputDir;
	}

	/**
	 * Sets the output directory for this project.
	 * @param outputDir - the outputDir to set
	 */
	public void setOutputDir(File outputDir) {
		this.outputDir = outputDir;
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
	 * Sets the ignoredFiles.
	 * @param ignoredFiles - the ignoredFiles to set
	 */
	public void setIgnoredFiles(Set<File> ignoredFiles) {
		this.ignoredFiles = ignoredFiles;
	}

	/**
	 * Load a configuration file.
	 * @param file - the file to be loaded
	 * @return the configuration
	 * @throws IOException when the file cannot be read
	 */
	public static Properties loadConfigFile(File file, Charset readEncoding) throws IOException {
		Properties rc = new Properties();
		if (file.exists()) {
			if (!file.canRead()) {
				throw new TemplatingException("Cannot read "+file.getCanonicalPath());
			}
			rc.load(new FileReader(file, readEncoding));
		}
		return rc;
	}
}
