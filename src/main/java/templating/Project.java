package templating;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

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
	
	private static SimpleDateFormat DATETIMEBUILDER = new SimpleDateFormat("yyyyMMddHHmmss");
	
	protected File projectRoot;
	
	/**
	 * Constructor.
	 */
	public Project(File projectRoot) {
		this.projectRoot = projectRoot;
	}

	/**
	 * Generate the project templates.
	 */
	public void generate() {
		try {
			log.info("Generating project "+projectRoot+"...");
			
			// Generating the output folder name
			String datetime = DATETIMEBUILDER.format(new Date());
			File outRoot = new File(projectRoot+"-"+datetime);
			
			// Recursively dive into the folder and generate the templates
			generateRecursively(null, projectRoot, outRoot);
			
			log.info("You will find your generated files in "+outRoot);
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
		Generator generator = new Generator(parent, dir, outDir);
		generator.run();
		for (File child : dir.listFiles()) {
			if (!child.getName().startsWith("__") && child.isDirectory() && child.canRead()) {
				generateRecursively(generator, child, new File(outDir, child.getName()));
			}
		}
	}
	
	
}
