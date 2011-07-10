package org.jside.webserver.test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import org.jside.webserver.RequestUtil;
import org.junit.Test;

public class HttpUtilTest {

	@Test
	public void testGetContentType() throws MalformedURLException, IOException {
		URL url =  new URL("http://my.baidu.com/data/json/today/");
		InputStream in =url.openStream();
		System.out.println(url.openConnection().getClass());
		System.out.println(in.getClass());
		char[] buf = new char[1024];
		InputStreamReader re = new InputStreamReader(in,"UTF-8");
		int c = 0;
		while((c = re.read(buf))>=0){
			System.out.println(new String(buf,0,c));
		}
		System.out.println(RequestUtil.getMimeType("aaa"));
	}

}
