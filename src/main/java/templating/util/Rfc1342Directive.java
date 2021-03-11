/**
 * 
 */
package templating.util;

import java.io.IOException;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.apache.commons.codec.net.QuotedPrintableCodec;

import freemarker.core.Environment;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

/**
 * Implements the quoted-printable conversion as described by RFC1342 (UTF-8 for email subjects).
 * @author ralph
 *
 */
public class Rfc1342Directive implements TemplateDirectiveModel {

	private boolean strict = true;
	
	/**
	 * Constructor for strict encoding.
	 */
	public Rfc1342Directive() {
		this(true);
	}

	/**
	 * Constructor.
	 * @param strict - use strict encoding
	 */
	public Rfc1342Directive(boolean strict) {
		this.strict = strict;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void execute(Environment env, Map params, TemplateModel[] loopVars, TemplateDirectiveBody body) throws TemplateException, IOException {
		// Check if no parameters were given:
		if (!params.isEmpty()) {
			throw new TemplateModelException("This directive doesn't allow parameters.");
		}
		if (loopVars.length != 0) {
			throw new TemplateModelException("This directive doesn't allow loop variables.");
		}

		// If there is non-empty nested content:
		if (body != null) {
			// Executes the nested body. Same as <#nested> in FTL, except
			// that we use our own writer instead of the current output writer.
			Rfc1342FilterWriter writer = new Rfc1342FilterWriter(env.getOut(), strict);
			writer.start();
			body.render(writer);
			writer.end();
			writer.flush();
		} else {
			throw new RuntimeException("missing body");
		}
	}

	/**
	 * A {@link Writer} that transforms the character stream to RFC1342 strings
	 * and forwards it to another {@link Writer}.
	 */
	private static class Rfc1342FilterWriter extends Writer {

		private Writer out;
		private QuotedPrintableCodec codec;

		Rfc1342FilterWriter(Writer out, boolean strict) {
			this.out      = out;
			codec         = new QuotedPrintableCodec(StandardCharsets.UTF_8, strict);
		}

		public void start() throws IOException {
			out.write("=?UTF-8?Q?");
		}

		public void end() throws IOException {
			out.write("?=");
		}

		public void write(char[] cbuf, int off, int len) throws IOException {
				String orig = new String(cbuf, off, len);
				// orig is in system default charset which can only be influenced outside of code (java -D flag)
				// The following lines try to fix it but still fail on Windows 10
				Charset internalDefault = Charset.defaultCharset();
				byte origBytes[] = orig.getBytes();
				CharBuffer utf8Buffer = internalDefault.decode(ByteBuffer.wrap(origBytes));
				String words = codec.encode(utf8Buffer.toString(), StandardCharsets.UTF_8);
				out.write(words);
		}

		public void flush() throws IOException {
			out.flush();
		}

		public void close() throws IOException {
			out.close();
		}
	}
}
