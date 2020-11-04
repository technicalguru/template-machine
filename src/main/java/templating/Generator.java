package templating;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Collection;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import templating.util.FileReadUtils;
import templating.util.GenerationInfo;
import templating.util.Rfc1342Directive;

/**
 * Generates all files in a directory (not recursively!)
 * @author ralph
 *
 */
public class Generator implements Runnable, TemplateLoader {

	/** The logger */
	public static Logger log = LoggerFactory.getLogger(Generator.class);

	protected Context                        context;
	//protected Generator                      parent;
	//protected GeneratorConfig                generatorConfig;
	//protected Properties                     templates;
	//protected Map<String,Properties>         localization;
	protected Configuration                  freemarkerConfig;
	protected long                           lastModified;
	protected GenerationInfo           info;
	/**
	 * Constructor.
	 */
	public Generator(Context context) {
		this.context          = context;
//		this.parent           = parent;
//		this.generatorConfig  = generatorConfig;
		this.lastModified     = System.currentTimeMillis();
		info                  = new GenerationInfo();

		// FreeMarker configuration is always specific to directory.
		freemarkerConfig = new Configuration(Configuration.VERSION_2_3_29);
		freemarkerConfig.setTemplateLoader(this);
		freemarkerConfig.setDefaultEncoding("UTF-8");
		freemarkerConfig.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
		freemarkerConfig.setLogTemplateExceptions(false);
		freemarkerConfig.setWrapUncheckedExceptions(true);
		freemarkerConfig.setFallbackOnNullLoopVariable(false);
		freemarkerConfig.setSharedVariable("quotedPrintable", new Rfc1342Directive(false));
		freemarkerConfig.setSharedVariable("qp", new Rfc1342Directive(false));
	}

	/**
	 * 
	 * {@inheritDoc}
	 */
	public void run() {
		info = new GenerationInfo();
		try {
			// Do only when we are in sub-folder (if configured)
			if (context.canGenerateDirectory()) {
				// Process each file now with each language
				for (File child : context.getSourceDir().listFiles()) {
					if (!context.isSpecialFile(child) && context.isValidFile(child) && child.isFile() && child.canRead()) {
						// Now for each language
						Collection<String> languages = context.getLanguages();
						info.addLanguages(languages);
						if (languages.size() > 1) {
							for (String language : languages) {
								File outFile = new File(new File(context.getOutputDir(), language), child.getName());
								generateFile(child, language, outFile);
								info.incFiles();
							}
						} else if (languages.size() > 0) {
							File outFile = new File(context.getOutputDir(), child.getName());
							generateFile(child, languages.iterator().next(), outFile);
							info.incFiles();
						} else {
							File outFile = new File(context.getOutputDir(), child.getName());
							generateFile(child, "default", outFile);
							info.incFiles();
						}
					}
				}
			}
		} catch (Throwable t) {
			throw new TemplatingException("Cannot generate files in "+context.getSourceDir().getPath(), t);
		}
	}

	/**
	 * Process a single template and generates the file.
	 * @param templateFile - the template file
	 * @param language     - the language to be used (cannot be null)
	 * @param outFile      - the output file
	 */
	protected void generateFile(File templateFile, String language, File outFile) throws IOException, TemplateException {
		// Ignore when the is a language specific template file
		File langTemplate = new File(new File(templateFile.getParentFile(), language), templateFile.getName());
		if (langTemplate.exists()) return;

		// Ignore when the template file is already language specific
		String parentName = templateFile.getParentFile().getName();
		if (context.hasLanguage(parentName)) {
			// Only process when the language is the same
			if (!parentName.equals(language)) return;
			// But we need to change the output file
			outFile = new File(outFile.getParentFile().getParentFile(), outFile.getName());
		}

		log.info("Generating "+outFile.getPath()+"...");
		// Ensure the parent dir exists
		FileUtils.forceMkdirParent(outFile);

		// Prepare localization
		Map<String,String> localization = context.getMergedLocalization(language);
		localization.put("templateAbsPath", templateFile.getCanonicalPath());
		localization.put("templateRelPath", context.getRelativePath(templateFile));

		// Generate
		Template temp = freemarkerConfig.getTemplate(templateFile.getName());
		FileWriter writer = new FileWriter(outFile);
		temp.process(localization, writer);
		writer.close();
	}

	/**
	 * Returns the info.
	 * @return the info
	 */
	public GenerationInfo getInfo() {
		return info;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object findTemplateSource(String name) throws IOException {
		if (context.getTemplate(name) == null) {
			// Exception: the template is the local file
			File f = new File(context.getSourceDir(), name);
			if (f.exists() && f.isFile() && f.canRead()) {
				return f;
			}
			return null;
		}
		return name;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getLastModified(Object templateSource) {
		return lastModified;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Reader getReader(Object templateSource, String encoding) throws IOException {
		if (templateSource instanceof File) {
			return FileReadUtils.getReader((File)templateSource, context.getReadEncoding());
		}
		return new StringReader(context.getTemplate(templateSource.toString()));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void closeTemplateSource(Object templateSource) throws IOException {
	}

}
