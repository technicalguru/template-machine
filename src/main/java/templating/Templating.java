/**
 * 
 */
package templating;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The main class for starting.
 * @author ralph
 *
 */
public class Templating {
	
	/** The logger */
	public static Logger log = LoggerFactory.getLogger(Templating.class);
	
	/**
	 * Main method.
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			// This is quick and dirty
			String projectDir   = "file:///c:/$User/SolutionDesign/BEX/email-templates-lx";
			File projectDirFile = new File(new URI(projectDir));
			if (!projectDirFile.isDirectory() || !projectDirFile.canRead()) throw new FileNotFoundException("Cannot read "+projectDir);
			Project project     = new Project(projectDirFile);
			project.generate();
		} catch (Throwable t) {
			log.error(t.getMessage(), t);
		}
	}

}
