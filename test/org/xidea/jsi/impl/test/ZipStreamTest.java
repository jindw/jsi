package org.xidea.jsi.impl.test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.junit.Test;

public class ZipStreamTest {
	@Test
	public void testStreamOpen() throws IOException{
		URL url = this.getClass().getResource("/org/xidea/lite/template.js");
	    InputStream in = url.openStream();
		System.out.println(url);
	}

}
