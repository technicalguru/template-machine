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
	protected TemplateMachineConfig templateMachineConfig;
	
	/**
	 * Constructor.
	 */
	public GeneratorConfig() {
	}

	/**
	 * Constructor.
	 * @param sourceDir        - the template source dir
	 * @param outputDir        - the output dir for the generated files
	 * @param templateMachineConfig - the templating config
	 */
	public GeneratorConfig(File sourceDir, File outputDir, TemplateMachineConfig templateMachineConfig) {
		this.sourceDir = sourceDir;
		this.outputDir = outputDir;
		this.templateMachineConfig = templateMachineConfig;
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
	 * Returns the templateMachineConfig.
	 * @return the templateMachineConfig
	 */
	public TemplateMachineConfig getTemplatingConfig() {
		return templateMachineConfig;
	}

	/**
	 * Sets the templateMachineConfig.
	 * @param templateMachineConfig - the templateMachineConfig to set
	 */
	public void setTemplatingConfig(TemplateMachineConfig templateMachineConfig) {
		this.templateMachineConfig = templateMachineConfig;
	}

	
}
