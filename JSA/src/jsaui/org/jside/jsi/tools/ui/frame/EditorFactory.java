package org.jside.jsi.tools.ui.frame;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.jside.ui.DesktopUtil;

public class EditorFactory {
	public static Editor createEditor(File file) {
		throw new RuntimeException();
	}

	private static Collection<String> jsexts = Arrays.asList("js", "sjs");

	private static Collection<String> xmlexts = Arrays.asList("xml", "xhtml",
			"html", "htm");
	
	private static final List<String> txts = Arrays.asList("txt","bat","sh","java","css");

	public static Editor createEditor(File file, String value, String encoding) {
		ContentArea editor = new ContentArea(value, encoding);
		String name = file.getName().toLowerCase();
		String ext = name.substring(name.lastIndexOf('.') + 1);
		HightLighter documentListener;
		if (jsexts.contains(ext)) {
			documentListener = HightLighter.createJSLighter(editor);
			editor.addAction(EditorCommand.FORMAT_JS_ACTION, "control shift F");
			editor.addAction(EditorCommand.COMPRESS_JS_ACTION,
					"control shift C");
		} else if (xmlexts.contains(ext)) {
			documentListener = HightLighter.createXMLLighter(editor);
		} else {
			//
			if(txts.contains(ext) || DesktopUtil.confirm("文件不被支持，是非继续打开？")){
				documentListener = HightLighter.createPlainLighter(editor);
			}else{
				return null;
			}
		}

		editor.addDocumentListener(documentListener);
		editor.addMouseListener(documentListener);
		
		ContentPopupMenu.addPopup(editor);
		editor.reset(file, null);
		return editor;
	}

}
