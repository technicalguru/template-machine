/**
 * 
 */
package templating;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

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
	@BeforeAll
	public static void generateValues() throws IOException {
		assertTrue(TEMPLATE_DIR.exists(), "TEMPLATE_DIR "+TEMPLATE_DIR.getAbsolutePath()+" cannot be found (Are you running outside of project dir?)");
		if (TARGET_DIR.exists()) FileUtils.deleteDirectory(TARGET_DIR);
		File configFile = new File(TEMPLATE_DIR, "template-machine.properties");
		Context rootContext = new Context(TEMPLATE_DIR, TARGET_DIR, TEMPLATE_DIR, TemplateMachine.load(configFile));
		rootContext.setReadEncoding(ENCODING);
		rootContext.setWriteEncoding(ENCODING);
		rootContext.ignoreFile(configFile);
		machine = new TemplateMachine(rootContext);
		info    = machine.generate();
		TARGET_DIR.deleteOnExit();
	}
	
	@Test
	public void testLanguageCount() {
		Collection<String> languages = info.getLanguages();
		assertEquals(3, languages.size(), "Number of generated languages is incorrect");
	}
	
	@Test
	public void testLanguageDE() {
		Collection<String> languages = info.getLanguages();
		assertTrue(languages.contains("de"), "German was not generated");
	}
	
	@Test
	public void testLanguageEN() {
		Collection<String> languages = info.getLanguages();
		assertTrue(languages.contains("en"), "English was not generated");
	}
	
	@Test
	public void testLanguageES() {
		Collection<String> languages = info.getLanguages();
		assertTrue(languages.contains("es"), "Spanish was not generated");
	}
	
	@Test
	public void testFilesCount() {
		assertEquals(12, info.getFiles(), "Invalid number of files generated");
	}
}
