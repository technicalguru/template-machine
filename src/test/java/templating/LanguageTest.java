/**
 * 
 */
package templating;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import templating.util.DirFinder;
import templating.util.GenerationInfo;

/**
 * Tests language features
 * @author ralph
 *
 */
public class LanguageTest {

	public static File TEMPLATE_DIR = null;
	public static File TARGET_DIR   = null;
	public static Charset ENCODING  = Charset.forName("UTF-8");

	protected static TemplateMachine machine;
	protected static GenerationInfo  info;
	
	static {
		try {
			TEMPLATE_DIR = new File(DirFinder.findDir("lang-data").toURI());
			TARGET_DIR   = new File("target"+System.getProperty("file.separator")+"lang-data");
		} catch (URISyntaxException e) {
			throw new RuntimeException("Cannot find TEMPLATE_DIR", e);
		}
	}
	
	
	/**
	 * Generate the test.
	 */
	@BeforeClass
	public static void generateValues() throws IOException {
		assertTrue("TEMPLATE_DIR "+TEMPLATE_DIR.getAbsolutePath()+" cannot be found (Are you running outside of project dir?)", TEMPLATE_DIR.exists());
		if (TARGET_DIR.exists()) FileUtils.deleteDirectory(TARGET_DIR);
		File configFile = new File(TEMPLATE_DIR, "template-machine.properties");
		TemplateMachineConfig cfg = new TemplateMachineConfig(TEMPLATE_DIR, TARGET_DIR, TemplateMachine.load(configFile), new Date());
		cfg.setReadEncoding(ENCODING);
		cfg.setWriteEncoding(ENCODING);
		cfg.ignoreFile(configFile);
		machine = new TemplateMachine(cfg);
		info    = machine.generate();
		TARGET_DIR.deleteOnExit();
	}
	
	@Test
	public void testLanguageCount() {
		Collection<String> languages = info.getLanguages();
		assertEquals("Number of generated languages is incorrect", 3, languages.size());
	}
	
	@Test
	public void testLanguageDE() {
		Collection<String> languages = info.getLanguages();
		assertTrue("German was not generated", languages.contains("de"));
	}
	
	@Test
	public void testLanguageEN() {
		Collection<String> languages = info.getLanguages();
		assertTrue("English was not generated", languages.contains("en"));
	}
	
	@Test
	public void testLanguageES() {
		Collection<String> languages = info.getLanguages();
		assertTrue("Spanish was not generated", languages.contains("es"));
	}
	
	@Test
	public void testFilesCount() {
		assertEquals("Invalid number of files generated", 12, info.getFiles());
	}
}
