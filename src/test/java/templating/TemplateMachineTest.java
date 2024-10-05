/**
 * 
 */
package templating;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import templating.util.DirFinder;

/**
 * Tests the TemplateMachine features.
 * @author ralph
 *
 */
public class TemplateMachineTest {

	public static File TEMPLATE_DIR = null;
	public static File TARGET_DIR   = null;
	public static Charset ENCODING  = Charset.forName("UTF-8");
	
	// value1: expected: TBD               actual: ${value1}
	public static Pattern PATTERN   = Pattern.compile("([^:]*):\\s*expected:\\s*(.*)\\s*actual:\\s*(.*)");

	static {
		try {
			TEMPLATE_DIR = new File(DirFinder.findDir("data").toURI());
			TARGET_DIR   = new File("target/data2");
		} catch (URISyntaxException e) {
			throw new RuntimeException("Cannot find TEMPLATE_DIR", e);
		}
	}
	
	/**
	 * Generate the test.
	 */
	public static void generateValues() throws IOException {
		assertThat(TEMPLATE_DIR.exists()).isTrue().withFailMessage("TEMPLATE_DIR "+TEMPLATE_DIR.getAbsolutePath()+" cannot be found (Are you running outside of project dir?)");
		if (TARGET_DIR.exists()) FileUtils.deleteDirectory(TARGET_DIR);
		File configFile = new File(TEMPLATE_DIR, "template-machine.properties");
		Context rootContext = new Context(TEMPLATE_DIR, TARGET_DIR, TEMPLATE_DIR, TemplateMachine.load(configFile));
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
	@TestFactory
	public Collection<DynamicTest> provideTests() throws Exception {
		generateValues();
		
		List<DynamicTest> rc = new ArrayList<>();
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
	private void populateValues(List<DynamicTest> collection, File dir) throws Exception {
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
	private void populateValuesInFile(List<DynamicTest> collection, File file) throws Exception {
		int line = 1;
		for (String s : FileUtils.readLines(file, ENCODING)) {
			if (!s.startsWith("#") && !s.trim().isEmpty()) {
				collection.add(getDynamicTest(new ValueTest(file, line, s)));
			}
			line++;
		}
	}
	
	/**
	 * Creates a dynamic test using the header test case and the given test case option.
	 * @param valueTest
	 * @return dynamic test
	 */
	private DynamicTest getDynamicTest(ValueTest valueTest) {
		DynamicTest test = DynamicTest.dynamicTest("[line "+valueTest.lineNo+"]", () -> {
			try {
				Matcher matcher = PATTERN.matcher(valueTest.line); 
				if (matcher.matches()) {
					String valueName = matcher.group(1).trim();
					String expected  = matcher.group(2).trim();
					String actual    = matcher.group(3).trim();
					assertThat(actual).isEqualTo(expected).withFailMessage("[line "+valueTest.lineNo+"] "+valueName+" fails");
				}
			} finally {
			}
		});
		return test;
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
