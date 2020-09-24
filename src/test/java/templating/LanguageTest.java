/**
 * 
 */
package templating;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests language features
 * @author ralph
 *
 */
public class LanguageTest {

	public static File TEMPLATE_DIR = new File("src/test/lang-data");
	public static File TARGET_DIR   = new File("target/lang-data");
	public static Charset ENCODING  = Charset.forName("UTF-8");

	protected static Project project;
	
	/**
	 * Generate the test.
	 */
	@BeforeClass
	public static void generateValues() throws IOException {
		if (TARGET_DIR.exists()) FileUtils.deleteDirectory(TARGET_DIR);
		File configFile = new File(TEMPLATE_DIR, "template-machine.properties");
		TemplateMachineConfig cfg = new TemplateMachineConfig(TEMPLATE_DIR, TARGET_DIR, configFile, new Date());
		cfg.setReadEncoding(ENCODING);
		cfg.setWriteEncoding(ENCODING);
		project = new Project(cfg);
		project.generate();
		TARGET_DIR.deleteOnExit();
	}
	
	@Test
	public void testLanguageCount() {
		Collection<String> languages = project.info.getLanguages();
		assertEquals("Number of generated languages is incorrect", 3, languages.size());
	}
	
	@Test
	public void testLanguageDE() {
		Collection<String> languages = project.info.getLanguages();
		assertTrue("German was not generated", languages.contains("de"));
	}
	
	@Test
	public void testLanguageEN() {
		Collection<String> languages = project.info.getLanguages();
		assertTrue("English was not generated", languages.contains("en"));
	}
	@Test
	public void testLanguageES() {
		Collection<String> languages = project.info.getLanguages();
		assertTrue("Spanish was not generated", languages.contains("es"));
	}
}
