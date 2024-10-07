/**
 * 
 */
package templating;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import templating.util.DirFinder;

/**
 * Tests the TemplateMachine features with a sub dir only.
 * @author ralph
 *
 */
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
	@BeforeAll
	public static void generateValues() throws IOException {
		assertTrue(TEMPLATE_DIR.exists(), "TEMPLATE_DIR "+TEMPLATE_DIR.getAbsolutePath()+" cannot be found (Are you running outside of project dir?)");
		if (TARGET_DIR.exists()) FileUtils.deleteDirectory(TARGET_DIR);
		File configFile = new File(TEMPLATE_DIR, "template-machine.properties");
		Context rootContext = new Context(TEMPLATE_DIR, TARGET_DIR, SUB_DIR, TemplateMachine.load(configFile));
		rootContext.setReadEncoding(ENCODING);
		rootContext.setWriteEncoding(ENCODING);
		rootContext.ignoreFile(configFile);
		TemplateMachine machine = new TemplateMachine(rootContext);
		machine.generate();
		TARGET_DIR.deleteOnExit();
	}
	
	/**
	 * Collect all test data.
	 * @return a list of test data
	 * @throws Exception - when a problem occurs
	 */
	private static Stream<ValueTest> provideTestCases() throws Exception {
		generateValues();
		
		List<ValueTest> rc = new ArrayList<>();
		if (TARGET_DIR.isDirectory()) {
			populateValues(rc, TARGET_DIR);
		} else {
			throw new FileNotFoundException(TARGET_DIR.getCanonicalPath()+" does not exist");
		}
		return rc.stream();
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
	
	/**
	 * Test a single value.
	 */
	@ParameterizedTest
	@MethodSource("provideTestCases")
	public void testValue(ValueTest testCase) throws Exception {
		// Fail this test if "dir-2" is part of the file
		assertFalse(testCase.file.getCanonicalPath().contains("/dir-2/"), "sub-dir test failed. dir-2 is part of the value test");
		
		Matcher matcher = PATTERN.matcher(testCase.line); 
		if (matcher.matches()) {
			String valueName = matcher.group(1).trim();
			String expected  = matcher.group(2).trim();
			String actual    = matcher.group(3).trim();
			assertThat(actual).isEqualTo(expected).withFailMessage(testCase.file.getCanonicalPath()+" [line "+testCase.lineNo+"] "+valueName+" fails");
		}
	}
	
	@AfterAll
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
