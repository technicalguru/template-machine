package templating;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A project definition.
 * @author ralph
 *
 */
public class Project {

	/** The logger */
	public static Logger log = LoggerFactory.getLogger(Project.class);
	
	protected TemplateMachineConfig templateMachineConfig;
	protected ProjectInfo info;
	
	/**
	 * Constructor.
	 */
	public Project(TemplateMachineConfig templateMachineConfig) {
		this.templateMachineConfig = templateMachineConfig;
		this.info                  = new ProjectInfo();
	}

	/**
	 * Generate the project files.
	 */
	public void generate() {
		try {
			log.info("Generating project "+templateMachineConfig.getProjectRoot()+"...");
			
			// Encoding
			System.setProperty("file.encoding", templateMachineConfig.getReadEncoding().name());

			// Recursively dive into the folder and generate the templates
			generateRecursively(null, templateMachineConfig.getProjectRoot(), templateMachineConfig.getOutRoot());
			
			log.info("You will find your generated files in "+templateMachineConfig.getOutRoot());
		} finally {
			log.info("Done");
			
		}
	}
	
	/**
	 * Generate recursively
	 * @param parent - the parent generator to allow overriding templates and localizations
	 * @param sourceDir    - the directory to process
	 * @param outputDir - the output directory
	 */
	protected void generateRecursively(Generator parent, File sourceDir, File outputDir) {
		// Create the generator
		Generator generator = new Generator(parent, new GeneratorConfig(sourceDir, outputDir, templateMachineConfig));
		generator.run();
		for (File child : sourceDir.listFiles()) {
			if (!templateMachineConfig.isSpecialFile(child) && child.isDirectory() && child.canRead()) {
				generateRecursively(generator, child, new File(outputDir, child.getName()));
			}
		}
		info.add(generator);	
	}
}
