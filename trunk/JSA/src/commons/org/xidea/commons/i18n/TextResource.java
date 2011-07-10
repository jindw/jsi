package org.xidea.commons.i18n;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;

import org.mozilla.intl.chardet.nsDetector;
import org.mozilla.intl.chardet.nsICharsetDetectionObserver;
import org.mozilla.intl.chardet.nsPSMDetector;

public class TextResource {
	private static String ENCODES = "UTF-8|Big5|GB18030|GBK";

	private Locale locale;

	private String charset = null;

	private String[] probableCharsets;

	private String text;

	private byte[] data;

	private String asciiEncoding = "GBK";

	public TextResource(InputStream in, Locale locale,CharsetSelector selector) throws IOException {
		this.locale = locale;
		this.initialize(in,selector);
		if (charset == null) {
			if(selector == null){
				charset = probableCharsets[0];
			}else{
				charset =  selector.selectCharset(probableCharsets);
			}
		}
	}
	public String getEncoding(){
		return charset;
	}
	public static String getText(InputStream in, Locale locale,CharsetSelector selector){
		TextResource det;
		try {
			det = new TextResource(in,locale,selector);
			return det.getText();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
	}

	public String getText() {
		if (text == null) {
			try {
				text = new String(data, charset);
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);
			}
		}
		return text;
	}

	protected boolean initialize(InputStream in,CharsetSelector selector) throws IOException {
		charset = null;
		probableCharsets = null;
		data = null;
		int flag = nsPSMDetector.ALL;
		if ("zh".equals(locale.getLanguage())) {
			//System.out.println(locale);
			flag = nsPSMDetector.CHINESE;
		}
		// Initalize the nsDetector() ;
		nsDetector det = new nsDetector(flag);

		// Set an observer...
		// The Notify() will be called when a matching charset is found.

		det.Init(new nsICharsetDetectionObserver() {
			public void Notify(String charset) {
				TextResource.this.charset = charset;
			}
		});
		int len = 1;
		byte[] bufBytes = new byte[1024];
		int pos = bufBytes.length;
		boolean isAscii = true;
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		while (-1 != (len = in.read(bufBytes, 0, bufBytes.length)))
		{
			pos += len;
			// Check if the stream is only ascii.
			if (isAscii) {
				isAscii = det.isAscii(bufBytes, len);
			}
			// DoIt if non-ascii and not done yet.in,detectLength
			if (!isAscii && charset == null) {
				det.DoIt(bufBytes, len, false);
			}
			buffer.write(bufBytes, 0, len);
		}

		det.Done();
		this.data = buffer.toByteArray();
		
		if (charset == null) {
			if(isAscii){
				charset = asciiEncoding ;
				return true;
			}
			this.probableCharsets = det.getProbableCharsets();
			Arrays.sort(this.probableCharsets, new Comparator<String>() {
				public int compare(String o1, String o2) {
					return ENCODES.indexOf(o2) - ENCODES.indexOf(o1);
				}
			});
			//System.out.println(Arrays.asList(this.probableCharsets));
			return false;
		}

		//System.out.println(charset);
		return true;
	}
	public static TextResource create(InputStream in, Locale local,
			CharsetSelector cs) {
		try {
			return new TextResource(in,local,cs);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
