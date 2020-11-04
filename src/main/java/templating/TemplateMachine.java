/**
 * 
 */
package templating;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

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

import templating.util.GenerationInfo;

/**
 * The main class for templating.
 * @author ralph
 *
 */
public class TemplateMachine {
	
	/** The logger */
	public static Logger log = LoggerFactory.getLogger(TemplateMachine.class);
	
	private static SimpleDateFormat DATETIMEBUILDER = new SimpleDateFormat("yyyyMMddHHmmss");
	
	protected TemplateMachineConfig config;
	
	/**
	 * Constructor.
	 */
	public TemplateMachine(TemplateMachineConfig config) {
		this.config = config;
	}

	/**
	 * Generate the project files.
	 */
	public GenerationInfo generate() {
		try {
			log.info("Generating project "+config.getSourceDir()+"...");
			
			// Encoding
			System.setProperty("file.encoding", config.getReadEncoding().name());

			// Root Context
			Context rootContext = new Context(config.getSourceDir(), config.getOutputDir(), config.getSubDir(), config.getConfig());
			rootContext.setReadEncoding(config.getReadEncoding());
			rootContext.setWriteEncoding(config.getWriteEncoding());
			
			// Recursively dive into the folder and generate the templates
			GenerationInfo rc = generateRecursively(rootContext);
			log.info("You will find your generated files in "+config.getOutputDir());
			return rc;
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
	protected GenerationInfo generateRecursively(Context context) {
		// Create the generator
		Generator generator = new Generator(context);
		generator.run();
		GenerationInfo rc = generator.getInfo();
		for (File child : context.getSourceDir().listFiles()) {
			if (!context.isSpecialFile(child) && child.isDirectory() && child.canRead()) {
				Context childContext = new Context(context, child, new File(context.getOutputDir(), child.getName()));
				rc.add(generateRecursively(childContext));
			}
		}
		return rc;
	}
	
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
			
			// Read the configuration
			Properties config     = new Properties();
			String configFilename = cl.getOptionValue("c");
			File   configFile     = new File(projectDirFile, "template-machine.properties");
			if (configFilename != null) {
				configFile = new File(configFilename);
				if (!configFile.exists() || !configFile.isFile()) {
					throw new TemplatingException(configFilename+" does not exist");
				}
				config = load(configFile);
			}
			
			// Read config
			TemplateMachineConfig cfg = new TemplateMachineConfig(projectDirFile, outDirFile, config, generationTime);
			
			// The subdir if it exists
			String subDir   = cl.getOptionValue("s");
			if (subDir != null) {
				File subDirFile = new File(projectDirFile, subDir);
				if (!subDirFile.exists() || !subDirFile.isDirectory()) {
					throw new TemplatingException("Sub-directory "+subDirFile.getCanonicalPath()+" does not exist");
				}
				cfg.setSubDir(subDirFile);
			}
			
			// Reading encoding
			String readEncodingName = Charset.defaultCharset().name();
			if (cl.hasOption("r")) {
				readEncodingName = cl.getOptionValue("r");
			}
			Charset readEncoding = Charset.forName(readEncodingName);
			cfg.setReadEncoding(readEncoding);
			
			// Writing encoding
			String writeEncodingName = Charset.defaultCharset().name();
			if (cl.hasOption("w")) {
				writeEncodingName = cl.getOptionValue("w");
			}
			Charset writeEncoding = Charset.forName(writeEncodingName);
			cfg.setWriteEncoding(writeEncoding);
			
			// Now the project
			TemplateMachine machine = new TemplateMachine(cfg);
			
			// And run...
			machine.generate();
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

		option = new Option("s", "sub-dir", true, "sub directory to generate within project (optional)");
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

		option = new Option("c", "config", true, "configuration file (optional, defaults to template-machine.properties)");
		option.setRequired(false);
		option.setArgs(1);
		rc.addOption(option);

		return rc;
	}

	public static Properties load(File file) throws IOException {
		Properties rc = new Properties();
		rc.load(new FileReader(file));
		return rc;
	}
}
