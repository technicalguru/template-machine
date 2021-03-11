/**
 * 
 */
package templating.util;

import static org.junit.Assert.assertEquals;

import java.nio.charset.StandardCharsets;

import org.apache.commons.codec.net.QuotedPrintableCodec;
import org.junit.Test;

/**
 * Tests the Quoted Printable Codec
 * @author ralph
 *
 */
public class Rfc1342DirectiveTest {

	//@Test
	public void testPortuguese() throws Exception {
		String decoded = "Atualização do estado: A sua bagagem atrasada chegou ao aeroporto e está disponível para levantamento";
		String encoded = "Atualiza=C3=A7=C3=A3o do estado: A sua bagagem atrasada chegou ao aeroporto e est=C3=A1 dispon=C3=ADvel para levantamento";
		
		assertEquals("Portoguese encoding failed", encoded, encode(decoded));
	}
	
	protected static String encode(String s) throws Exception {
		//Rfc1342Directive encoder = new Rfc1342Directive();
		QuotedPrintableCodec codec = new QuotedPrintableCodec(StandardCharsets.UTF_8, false);
		String rc = codec.encode(s);
		//rc = rc.replace(" ", "_");
		System.out.println(s);
		System.out.println(rc);
		return rc;
	}
}
