package org.jside.jsi.tools.ui.frame;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.text.Highlighter.HighlightPainter;

public class HightLighter extends MouseAdapter implements DocumentListener {

	static final String JS_COMMENT = "/\\*[\\s\\S]*?\\*/|\\/\\/.*";
	static final String STRING_PATTERN = "\"(?:\\\\.|[^\"\r\n])*\"|'(?:\\\\.|[^'\r\n])*'";
	static final String JS_KEYWORD = "\\bvar\\b|\\bfunction\\b|\\bthis\\b|\\bprototype\\b|\\barguments\\b";

	public static final Pattern JS_GROUPS = Pattern.compile(STRING_PATTERN
			+ '|' + JS_COMMENT + '|' + JS_KEYWORD, Pattern.MULTILINE);

	public static final Pattern XML_GROUPS = Pattern
			.compile("<script\\b[\\s\\S]*?</script>|<style\\b[\\s\\S]*?</style>"
					+ "|<!\\[CDATA\\[[\\s\\S]*?\\]\\]>|<[^>]+>");

	public static HightLighter createJSLighter(JTextArea textArea) {
		HightLighter lighter = new HightLighter(textArea, JS_GROUPS, jsLighter);
		return lighter;
	}

	public static HightLighter createXMLLighter(JTextArea textArea) {
		HightLighter lighter = new HightLighter(textArea, XML_GROUPS,
				xmlLighter);
		return lighter;
	}

	public static HightLighter createPlainLighter(JTextArea textArea) {
		HightLighter lighter = new HightLighter(textArea, null, xmlLighter);
		return lighter;
	}

	static MatcherHightLighter jsLighter = new MatcherHightLighter();
	static MatcherHightLighter xmlLighter = new MatcherHightLighter() {
		private Pattern attributeGroup = Pattern
				.compile("([\\w\\:\\-]+)(?:\\s*=\\s*"
						+ "(\"[^\"]*\"|'[^']*'))?");

		private HighlightPainter cdataPainter = new DefaultHighlighter.DefaultHighlightPainter(
				new Color(0xEEEEEE));
		private HighlightPainter tagPainter = new DefaultHighlighter.DefaultHighlightPainter(
				new Color(208, 222, 255));
		private HighlightPainter scriptPainter = new DefaultHighlighter.DefaultHighlightPainter(
				new Color(222, 152, 255));
		private HighlightPainter attrNamePainter = new DefaultHighlighter.DefaultHighlightPainter(
				new Color(0xEEDDDD));
		private HighlightPainter attrValuePainter = new DefaultHighlighter.DefaultHighlightPainter(
				new Color(200, 255, 200));

		public void doHighLight(Highlighter hl, Matcher result)
				throws BadLocationException {

			int start = result.start();
			int end = result.end();
			String part = result.group();
			int p0 = part.charAt(0);
			if (p0 == '<') {// xml
				int p1 = part.charAt(1);
				if (p1 == '!') {
					if (part.charAt(1) == '-') {
						hl.addHighlight(start, end, cdataPainter);
					} else if (part.startsWith("<![CDATA[")) {
						hl.addHighlight(start, end, commentKeyPainter);
					} else {
						hl.addHighlight(start, end, cdataPainter);
					}
				} else if (part.endsWith("</script>")) {
					hl.addHighlight(start, end, scriptPainter);
				} else if (part.endsWith("</style>")) {
					hl.addHighlight(start, end, scriptPainter);
				} else {
					Matcher result2 = attributeGroup.matcher(part);
					while (result2.find()) {
						if (result2.groupCount() == 2 && result2.start(2) > 0) {
							hl.addHighlight(start + result2.start(1), start
									+ result2.end(1), attrNamePainter);

							hl.addHighlight(start + result2.start(2), start
									+ result2.end(2), attrValuePainter);
						} else {
							hl.addHighlight(start + result2.start(1), start
									+ result2.end(1), tagPainter);
						}
					}

				}
			}
		}
	};

	static class MatcherHightLighter {

		public static final Set<String> JS_FLAG_GROUPS = new HashSet<String>(
				Arrays.asList("var", "function", "this", "prototype"));
		private Set<String> flagKeys = JS_FLAG_GROUPS;

		protected HighlightPainter commentKeyPainter = new DefaultHighlighter.DefaultHighlightPainter(
				new Color(0xDDDDDD));
		protected HighlightPainter stringKeyPainter = new DefaultHighlighter.DefaultHighlightPainter(
				new Color(180, 190, 255));// 128, 128, 255));//200, 255, 200
		protected HighlightPainter flagKeyPainter = new DefaultHighlighter.DefaultHighlightPainter(
				new Color(181, 152, 212));

		public void doHighLight(Highlighter hl, Matcher result)
				throws BadLocationException {
			int start = result.start();
			int end = result.end();
			String part = result.group();
			if (flagKeys.contains(part)) {
				hl.addHighlight(start, end, flagKeyPainter);
			} else {
				int p0 = part.charAt(0);
				if (p0 == '/') {
					hl.addHighlight(start, end, commentKeyPainter);
				} else if (p0 == '"' || p0 == '\'') {
					hl.addHighlight(start, end, stringKeyPainter);
				}
			}
		}
	}

	private static HighlightPainter SELECTED_PAINTER = new DefaultHighlighter.DefaultHighlightPainter(
			Color.YELLOW);
	public Pattern groups;
	private HighlightPainter selectedPainter = SELECTED_PAINTER;

	private JTextArea textArea;
	private MatcherHightLighter lighter;

	public HightLighter(JTextArea textArea, Pattern groups,
			MatcherHightLighter lighter) {
		this.groups = groups;
		this.textArea = textArea;
		this.lighter = lighter;
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		updateHightLighter();

	}

	public void changedUpdate(DocumentEvent e) {
		updateHightLighter();

	}

	public void insertUpdate(DocumentEvent e) {
		updateHightLighter();
	}

	public void removeUpdate(DocumentEvent e) {
		updateHightLighter();
	}

	private void updateHightLighter() {
		Highlighter hl = textArea.getHighlighter();
		String text = textArea.getText();
		// String selectedKeyword = findNearKeyword(text);
		hl.removeAllHighlights();
		try {
			if (text.length() > 0) {
				String key = findNearKeyword(text);
				if (key.length() > 0) {
					int p = text.indexOf(key);
					while (p > 0) {
						int end = p + key.length();
						hl.addHighlight(p, end, selectedPainter);
						p = text.indexOf(key, end);
					}
				}
				if (groups != null) {
					Matcher result = groups.matcher(text);
					while (result.find()) {
						lighter.doHighLight(hl, result);
					}
				}
			}
		} catch (BadLocationException e) {
			e.printStackTrace();
		}

	}

	private String findNearKeyword(String text) {
		String selected = textArea.getSelectedText();
		if (selected == null || (selected = selected.trim()).length() == 0) {
			int hit = textArea.getSelectionStart();
			int count = text.length();
			if (hit < count && hit >= 0) {
				int begin = hit;
				while (begin > 0) {
					begin--;
					if (!Character.isJavaIdentifierPart(text.charAt(begin))) {
						begin++;
						break;
					}
				}
				int end = hit;
				while (++end < count) {
					if (!Character.isJavaIdentifierPart(text.charAt(end))) {
						break;
					}
				}
				selected = text.substring(begin, end);
			} else {
				selected = "";
			}
		}
		return selected;
	}
}
