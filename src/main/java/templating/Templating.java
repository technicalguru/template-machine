/**
 * 
 */
package templating;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The main class for templating.
 * @author ralph
 *
 */
public class Templating {
	
	/** The logger */
	public static Logger log = LoggerFactory.getLogger(Templating.class);
	
	private static SimpleDateFormat DATETIMEBUILDER = new SimpleDateFormat("yyyyMMddHHmmss");
	
	/**
	 * Main method.
	 * @param args - arguments will be written on command line when called without any option
	 */
	public static void main(String[] args) {
		try {
			Date generationTime      = new Date();

			// Parse the command line
			CommandLineParser parser = new DefaultParser();
			CommandLine cl           = parser.parse(getCommandLineOptions(), args);
			
			// The template directory
			String projectDir   = cl.getOptionValue("t");
			File projectDirFile = new File(projectDir);
			if (!projectDirFile.isDirectory() || !projectDirFile.canRead()) throw new FileNotFoundException("Cannot read "+projectDir);
			
			// The output directory
			String outDir   = cl.getOptionValue("o");
			if (outDir == null) {
				// Generating the output folder name
				String datetime = DATETIMEBUILDER.format(generationTime);
				outDir = projectDir+"-"+datetime;
				
			}
			
			// Handle any existing output directory
			File outDirFile = new File(outDir);
			if (outDirFile.exists()) {
				if (!cl.hasOption("f")) {
					throw new TemplatingException("Output directory already exists. Use -f option to force overwriting");
				} else {
					File oldDir = new File(outDirFile.getAbsolutePath()+".old");
					if (oldDir.exists()) FileUtils.deleteDirectory(oldDir);
					outDirFile.renameTo(oldDir);
					log.info("Moved existing putput directory to "+oldDir.getAbsolutePath());
				}
			}
			
			// Reading encoding
			String readEncodingName = Charset.defaultCharset().name();
			if (cl.hasOption("r")) {
				readEncodingName = cl.getOptionValue("r");
			}
			Charset readEncoding = Charset.forName(readEncodingName);
			
			// Writing encoding
			String writeEncodingName = Charset.defaultCharset().name();
			if (cl.hasOption("w")) {
				writeEncodingName = cl.getOptionValue("w");
			}
			Charset writeEncoding = Charset.forName(writeEncodingName);
			
			// Now the project
			Project project     = new Project(projectDirFile, outDirFile, generationTime);
			project.setReadEncoding(readEncoding);
			project.setWriteEncoding(writeEncoding);
			
			// And run...
			project.generate();
		} catch (MissingOptionException e) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("templating", getCommandLineOptions());
		} catch (Throwable t) {
			log.error(t.getMessage(), t);
		}
	}

	/**
	 * Creates the command line options.
	 * @return CL options object
	 */
	protected static Options getCommandLineOptions() {
		Options rc = new Options();
		Option option = null;

		option = new Option("t", "template-dir", true, "template directory");
		option.setRequired(true);
		option.setArgs(1);
		rc.addOption(option);

		option = new Option("o", "output-dir", true, "output directory (optional)");
		option.setRequired(false);
		option.setArgs(1);
		rc.addOption(option);

		option = new Option("f", "force", true, "overwrite existing output directory (optional)");
		option.setRequired(false);
		option.setArgs(0);
		rc.addOption(option);

		option = new Option("r", "read-encoding", true, "encoding of templates (optional, defaults to platform)");
		option.setRequired(false);
		option.setArgs(1);
		rc.addOption(option);

		option = new Option("w", "write-encoding", true, "encoding for generates files (optional, defaults to platform)");
		option.setRequired(false);
		option.setArgs(1);
		rc.addOption(option);

		return rc;
	}


}
