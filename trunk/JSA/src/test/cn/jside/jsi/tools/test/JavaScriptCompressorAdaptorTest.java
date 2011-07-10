package cn.jside.jsi.tools.test;

import java.util.regex.Pattern;

import org.junit.Test;


public class JavaScriptCompressorAdaptorTest {
	@Test
	public void testMatch(){
		String result = "\\u2ff345.</script>..金大为";
		System.out.println(Pattern.compile("\\\\u[\\da-fA-F]{4}").matcher(result).find());
		System.out.println(Pattern.compile("[\\u0080-\\uFFFF]").matcher(result).find());
		System.out.println(Pattern.compile("<\\/script>", Pattern.CASE_INSENSITIVE)
		.matcher(result).replaceAll("<\\\\/script>"));

		for(int i = 0;i<=0x1f;i++){
			System.out.println(Character.getType(i));
		}
		for(int i = 0x7f;i<=0x9f;i++){
			System.out.println(Character.getType(i));
		}

		System.out.println(Character.getType(0x80));
		System.out.println(Character.getType(0xFFFF));
		System.out.println(Character.getType('\r'));
		System.out.println(Character.getType('\n'));
		System.out.println(Character.getType('\\'));
		//\\\x00-\x1f\x7f-\x9f
	}

}
