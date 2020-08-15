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
	
	protected TemplatingConfig config;
	
	/**
	 * Constructor.
	 */
	public Project(TemplatingConfig config) {
		this.config = config;
	}

	/**
	 * Generate the project files.
	 */
	public void generate() {
		try {
			log.info("Generating project "+config.getProjectRoot()+"...");
			
			// Encoding
			System.setProperty("file.encoding", config.getReadEncoding().name());

			// Recursively dive into the folder and generate the templates
			generateRecursively(null, config.getProjectRoot(), config.getOutRoot());
			
			log.info("You will find your generated files in "+config.getOutRoot());
		} finally {
			log.info("Done");
			
		}
	}
	
	/**
	 * Generate recursively
	 * @param parent - the parent generator to allow overriding templates and localizations
	 * @param dir    - the directory to process
	 * @param outDir - the output directory
	 */
	protected void generateRecursively(Generator parent, File dir, File outDir) {
		// Create the generator
		Generator generator = new Generator(parent, dir, outDir, config);
		generator.run();
		for (File child : dir.listFiles()) {
			if (!child.getName().startsWith("__") && child.isDirectory() && child.canRead()) {
				generateRecursively(generator, child, new File(outDir, child.getName()));
			}
		}
	}
	
	/**
	 * Returns true when a file (template or localization) can be used for templating.
	 * <p>This is being used for .bak, ~ or .swap files (temporary and backup files).</p>
	 * @param file - the file to be checked
	 * @return {@code true} when file can be used in template reading
	 */
	public static boolean isValidFile(File file) {
		String name = file.getName();
		if (name.startsWith(".")) return false;
		if (name.endsWith("~")) return false;
		if (name.endsWith(".bak")) return false;
		return true;
	}
}
