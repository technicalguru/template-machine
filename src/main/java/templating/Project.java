package templating;

import java.io.File;
import java.nio.charset.Charset;
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
	
	protected File    projectRoot;
	protected File    outRoot;
	protected Date    generationTime;
	protected Charset readEncoding  = Charset.defaultCharset();
	protected Charset writeEncoding = Charset.defaultCharset();
	
	/**
	 * Constructor.
	 */
	public Project(File projectRoot, File outRoot, Date generationTime) {
		this.projectRoot    = projectRoot;
		this.outRoot        = outRoot;
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
	 * @param readEncoding the readEncoding to set
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
	 * @param writeEncoding the writeEncoding to set
	 */
	public void setWriteEncoding(Charset writeEncoding) {
		this.writeEncoding = writeEncoding;
	}

	/**
	 * Generate the project files.
	 */
	public void generate() {
		try {
			log.info("Generating project "+projectRoot+"...");
			
			// Encoding
			System.setProperty("file.encoding", readEncoding.name());

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
		Generator generator = new Generator(parent, dir, outDir, readEncoding, writeEncoding, generationTime);
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
