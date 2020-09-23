package templating;

import java.io.File;

/**
 * The configuration for the {@link Generator} class.
 * @author ralph
 *
 */
public class GeneratorConfig {

	protected File sourceDir;
	protected File outputDir;
	protected TemplatingConfig templatingConfig;
	
	/**
	 * Constructor.
	 */
	public GeneratorConfig() {
	}

	/**
	 * Constructor.
	 * @param sourceDir        - the template source dir
	 * @param outputDir        - the output dir for the generated files
	 * @param templatingConfig - the templating config
	 */
	public GeneratorConfig(File sourceDir, File outputDir, TemplatingConfig templatingConfig) {
		this.sourceDir = sourceDir;
		this.outputDir = outputDir;
		this.templatingConfig = templatingConfig;
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
	 * Returns the templatingConfig.
	 * @return the templatingConfig
	 */
	public TemplatingConfig getTemplatingConfig() {
		return templatingConfig;
	}

	/**
	 * Sets the templatingConfig.
	 * @param templatingConfig - the templatingConfig to set
	 */
	public void setTemplatingConfig(TemplatingConfig templatingConfig) {
		this.templatingConfig = templatingConfig;
	}

	
}
