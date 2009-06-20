package org.xidea.jsi.web.test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;
import org.xidea.jsi.web.JSIService;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;


public class JSIServiceTest {
	private BASE64Encoder encoder = new BASE64Encoder();
	private BASE64Decoder decoder = new BASE64Decoder();
	private JSIService service = new JSIService();
	

	@Test
	public void testExecute() throws IOException {

		testData("aaaa");
		testData("13333a");
		testData("13333ahefieodwaiueiamfnaehry--oiw=");
		testData("1");
		testData("12");
		testData("123");
		testData("1234");
		testData("12121213");
		testData("1212124313");
		testData("1212134535213");
		testData("123");
		testData("1444");
		testRadom();
		for (int i = 0; i <1000; i++) {
			testRadom();
		}
	}


	private void testRadom() throws IOException {
		int p = (int)(Math.random() *100);
		StringBuilder buf = new StringBuilder();
		for(int i=0;i<p;i++){
			buf.append((char)(Math.random() *256));
		}
		testData(buf.toString());
		
	}


	private void testData(String input) throws IOException {
		String base64 = encoder.encode(input.getBytes());
		//System.out.println(input);
		//System.out.println(base64);
		ByteArrayOutputStream out1 = new ByteArrayOutputStream();
		service.writeBase64(base64, out1);
		byte[] outbyte2 = decoder.decodeBuffer(base64);
		//System.out.println(new String(outbyte2));
		//System.out.println(new String(out1.toByteArray()));
		Assert.assertArrayEquals(outbyte2,out1.toByteArray());
	}

}
