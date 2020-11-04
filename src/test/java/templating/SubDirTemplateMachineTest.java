/**
 * 
 */
package templating;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import templating.util.DirFinder;

/**
 * Tests the TemplateMachine features with a sub dir only.
 * @author ralph
 *
 */
@RunWith(Parameterized.class)
public class SubDirTemplateMachineTest {

	public static File TEMPLATE_DIR = null;
	public static File SUB_DIR      = null;
	public static File TARGET_DIR   = null;
	public static Charset ENCODING  = Charset.forName("UTF-8");
	
	// value1: expected: TBD               actual: ${value1}
	public static Pattern PATTERN   = Pattern.compile("([^:]*):\\s*expected:\\s*(.*)\\s*actual:\\s*(.*)");

	static {
		try {
			TEMPLATE_DIR = new File(DirFinder.findDir("data").toURI());
			SUB_DIR      = new File(TEMPLATE_DIR, "dir-1");
			TARGET_DIR   = new File("target"+System.getProperty("file.separator")+"data");
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
		cfg.setSubDir(SUB_DIR);
		TemplateMachine machine = new TemplateMachine(cfg);
		machine.generate();
		TARGET_DIR.deleteOnExit();
	}
	
	/**
	 * Collect all test data.
	 * @return a list of test data
	 * @throws Exception - when a problem occurs
	 */
	@Parameters
	public static Collection<ValueTest> values() throws Exception {
		generateValues();
		
		List<ValueTest> rc = new ArrayList<>();
		if (TARGET_DIR.isDirectory()) {
			populateValues(rc, TARGET_DIR);
		} else {
			throw new FileNotFoundException(TARGET_DIR.getCanonicalPath()+" does not exist");
		}
		return rc;
	}
	
	/**
	 * Recursively collect directories to check for the values.
	 * @param collection - the collection for the values
	 * @param dir        - the directory to climb down
	 * @throws Exception - when a problem occurs
	 */
	private static void populateValues(List<ValueTest> collection, File dir) throws Exception {
		for (File child : dir.listFiles()) {
			if (child.isDirectory()) {
				populateValues(collection, child);
			} else if (child.isFile()) {
				populateValuesInFile(collection, child);
			}
		}
	}

	/**
	 * Read the file and populate the collection with valid lines
	 * @param collection - the collection for the values
	 * @param file       - the file to read
	 * @throws Exception - when a problem occurs
	 */
	private static void populateValuesInFile(List<ValueTest> collection, File file) throws Exception {
		int line = 1;
		for (String s : FileUtils.readLines(file, ENCODING)) {
			if (!s.startsWith("#") && !s.trim().isEmpty()) {
				collection.add(new ValueTest(file, line, s));
			}
			line++;
		}
	}
	
	private ValueTest testCase;
	
	/**
	 * Constructor for parameterized test.
	 * @param testCase - the test case
	 */
	public SubDirTemplateMachineTest(ValueTest testCase) {
		this.testCase = testCase;
	}
	
	/**
	 * Test a single value.
	 */
	@Test
	public void testValue() throws Exception {
		// Fail this test if "dir-2" is part of the file
		assertFalse("sub-dir test failed. dir-2 is part of the value test", testCase.file.getCanonicalPath().contains("/dir-2/"));
		
		Matcher matcher = PATTERN.matcher(testCase.line); 
		if (matcher.matches()) {
			String valueName = matcher.group(1).trim();
			String expected  = matcher.group(2).trim();
			String actual    = matcher.group(3).trim();
			assertEquals(testCase.file.getCanonicalPath()+" [line "+testCase.lineNo+"] "+valueName+" fails - ", expected, actual);
		}
	}
	
	@AfterClass
	public static void cleanup() {
		TARGET_DIR.delete();
	}
	
	/**
	 * Reflects a single test line in a file.
	 * @author ralph
	 *
	 */
	protected static class ValueTest {
		
		protected File   file;
		protected int    lineNo; 
		protected String line;
		
		public ValueTest(File file, int lineNo, String line) {
			this.file   = file;
			this.lineNo = lineNo;
			this.line   = line;
		}
	}
}
