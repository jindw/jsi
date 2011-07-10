package cn.jside.jsi.tools;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.regex.Pattern;

import org.jside.jsi.tools.JavaScriptAnalysisResult;
import org.jside.jsi.tools.JavaScriptCompressionAdvisor;
import org.jside.jsi.tools.JavaScriptCompressor;
import org.jside.jsi.tools.JavaScriptCompressorConfig;

import cn.jside.jsi.tools.rhino.RhinoCompressor;

public class JavaScriptCompressorAdaptor implements JavaScriptCompressor {
	public final static JavaScriptCompressionAdvisor EMPTY_ADVISOR = new JavaScriptCompressionAdvisor() {
		public String getReplacedName(String oldValue, boolean external) {
			return oldValue;
		}

		public String newVaribaleName() {
			throw new UnsupportedOperationException();
		}
	};
	private final Object lock = new Object();
	// JavaScriptCompressor
	private RhinoCompressor compressor = new RhinoCompressor();
	private float rate = 1;
	private JavaScriptCompressorConfig config;

	public JavaScriptAnalysisResult analyse(String source) {
		synchronized (lock) {
			try{
			return compressor.analyse(source);
			}catch(RuntimeException e){
				System.out.println("问题代码:"+source);
				throw e;
			}
		}
	}

	public String format(String script) {
		synchronized (lock) {
			String result = compressor.format(script);
			result = asciiUnescape(result);
			this.rate = result.length() / script.length();
			return result;
		}
	}
	static CharsetEncoder GB2312  = Charset.forName("GB2312").newEncoder();
	public static boolean isSourceable(char c){
		if ((c <= 0x001F) || (c >= 0x007F && c <= 0x009F)) {
			return false;
		}
		switch(Character.getType(c)){
		case Character.CONTROL:
		case Character.UNASSIGNED:
			return false;
		}
		if(GB2312.canEncode(c)){
			return true;
		}
		return false;
	}
	private Pattern unicodePattern = Pattern.compile("\\\\u[\\da-fA-F]{4}");
	
	private Pattern nativePattern = Pattern.compile("[\\u0080-\\uFFFF]");
	
	private Pattern escapeScriptTagPattern = Pattern.compile("</script>", Pattern.CASE_INSENSITIVE);
	
	public String compress(String source, JavaScriptCompressionAdvisor advisor) {
		if(source==null && source.length() ==0){
			//throw new RuntimeException("源代码不能为空");
			this.rate = 0;
			return source;
		}
		synchronized (lock) {
			if (advisor == null) {
				advisor = EMPTY_ADVISOR;
			}
			String result = compressor.compress(source, advisor);
			if (config.isEscapeScriptTag()) {
				result = 
					escapeScriptTagPattern.matcher(result).replaceAll("<\\\\/script>");
			}
			if (config.isAscii()) {
				result = asciiEscape(result);
			} else {
				result = asciiUnescape(result);
			}
			this.rate = getUTF8Length(result) / getUTF8Length(source);
			return result;
		}
	}

	private String asciiUnescape(String result) {
		if (unicodePattern.matcher(result).find()) {
			int length = result.length();
			int checkLength = length-5;
			StringBuilder buf = new StringBuilder(checkLength );
			int i = 0;
			for (; i <checkLength ; i++) {
				char c = result.charAt(i);
				if(c == '\\' && result.charAt(i+1) == 'u'){
					String number = result.substring(i+2, i+6);
					try{
						char c2 = (char)Integer.parseInt(number,16);
						if(c2 == '\\'){
							buf.append("\\\\");
						}else if (c2>=0x80 && isSourceable(c2)){
							buf.append(c2);
						}else{
							buf.append("\\u");
							buf.append(number);
						}
						i+=5;
						continue;
					}catch (Exception e) {
					}
					
				}
				buf.append(c);
			}
			if ( i <length ) {
				buf.append(result.substring(i));
			}
			result = buf.toString();
		}
		return result;
	}

	private String asciiEscape(String result) {
		if (nativePattern.matcher(result).find()) {
			int length = result.length();
			StringBuilder buf = new StringBuilder(length + length/4 );
			for (int i = 0; i <length ; i++) {
				char c = result.charAt(i);
				if(c<0x80){
					buf.append(c);
				}else{
					buf.append("\\u");
					buf.append(Integer.toHexString(0x10000+c).substring(1));
				}
			}
			result = buf.toString();
		}
		return result;
	}

	public float getLatestRate() {
		return this.rate;
	}

	public void setCompressorConfig(JavaScriptCompressorConfig config) {
		synchronized (lock) {
			this.config = config;
			compressor.setCompressorConfig(config);
		}
	}

	private int getUTF8Length(String s) {
		int result = 0;
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if ((c >= 0x0001) && (c <= 0x007F)) {
				result++;
			} else if (c > 0x07FF) {
				result += 3;
			} else {
				result += 2;
			}
		}
		return result;
	}
}
