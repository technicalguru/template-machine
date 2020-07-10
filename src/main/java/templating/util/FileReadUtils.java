package templating.util;

import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;


/**
 * Helper methods to correctly read Unicode files if required
 * @author ralph
 *
 */
public class FileReadUtils {

	/**
	 * Returns a reader that is able to handle BOM UTF-8 files.
	 * @param file     - the file to be read
	 * @param encoding - the encoding
	 * @return the reader
	 */
	public static Reader getReader(File file, String encoding) throws IOException {
		return getReader(file, Charset.forName(encoding));
	}

	/**
	 * Returns a reader that is able to handle BOM UTF-8 files.
	 * @param file     - the file to be read
	 * @param encoding - the encoding
	 * @return the reader
	 */
	public static Reader getReader(File file, Charset encoding) throws IOException {
		Reader rc = null;
		if (encoding.name().toLowerCase().startsWith("utf-")) {
			UnicodeInputStream in = new UnicodeInputStream(new FileInputStream(file), encoding.name());
			in.init();
			rc = new InputStreamReader(in, encoding);
		} else {
			rc = new InputStreamReader(new FileInputStream(file), encoding);
		}

		return rc;
	}

	/**
	 * Reads a file into a string handling UTF-8-BOM correctly.
	 * @param file     - the file to be read
	 * @param encoding - the encoding
	 * @return the contents of the file
	 * @throws IOException
	 */
	public static String readFile(File file, Charset encoding) throws IOException {
		CharArrayWriter writer = null;
		Reader          reader = null;
		try {
			reader = getReader(file, encoding);
			writer = new CharArrayWriter();
			char[] buffer = new char[64 * 1024];
			int read;
			while( (read = reader.read(buffer)) != -1) {
				writer.write(buffer, 0, read);
			}
			writer.flush();
			return new String(writer.toCharArray());
		} finally {
			reader.close();
			writer.close();
		}
	}
}
