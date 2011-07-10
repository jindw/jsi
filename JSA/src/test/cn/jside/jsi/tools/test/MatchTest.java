package cn.jside.jsi.tools.test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

public class MatchTest {
	private static final Pattern KEYWORD = Pattern.compile(
			"\"(?:\\\\.|[^\"\r\n])*\"|'(?:\\\\.|[^'\r\n])*'|/\\*.*?\\*/|\\/\\/.*"
	+ "|\\bvar\\b|\\bfunction\\b|\\bthis\\b|\\bprototype\\b",
			Pattern.MULTILINE);

	@Test
	public void restMatch(){
		Matcher result = KEYWORD.matcher("var function pro");
		while(result.find()) {
			System.out.println(result.group());
		}
	}

}
