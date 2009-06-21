package org.xidea.jsi.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.rmi.server.SocketSecurityException;

import org.junit.Test;

public class HttpTest {
	@Test
	public void testHttpPost() throws Exception {
//		new Thread() {
//			public void run() {
//				try {
//					ServerSocket socketServer = new ServerSocket(8088);
//					System.out.println("begin");
//					Socket socket = socketServer.accept();
//					System.out.println("accepted");
//					InputStreamReader in = new InputStreamReader(socket.getInputStream());
//					int b;
//					System.out.println("recived");
//					while ((b = in.read()) >= 0) {
//						System.out.print((char) b);
//					}
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//		}.start();
		//String url = "http://localhost:8088/";
		String url = "http://www.xidea.org/";
		HttpURLConnection http = (HttpURLConnection) new URL(url)
				.openConnection();
		http.setRequestMethod("POST");
		http.setRequestProperty("Content-Length", "0");
		// http.connect();
		System.out.println(http.getResponseMessage());
		print(http.getInputStream());

	}
	public void print(InputStream in) throws IOException{
		InputStreamReader reader = new InputStreamReader(in);
		int b;
		System.out.println("recived");
		while ((b = reader.read()) >= 0) {
			System.out.print((char) b);
		}
	}

}
