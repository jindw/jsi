package org.jside.jsi.tools.ui.frame;

import java.awt.BorderLayout;
import java.awt.Color;
import java.io.IOException;
import java.net.URL;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.jside.ui.DesktopUtil;
import org.xidea.commons.i18n.TextResource;
import org.xidea.commons.i18n.CharsetSelector;
import org.xidea.commons.i18n.DefaultCharsetSelector;

public class ADPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	public ADPanel(){
		// topAds.setBackground(new Color(100,0,0));
		JEditorPane content = new JEditorPane();
		this.setLayout(new BorderLayout());
		this.add(content, BorderLayout.CENTER);
	
		content.setContentType("text/html");//$NON-NLS-1$
		content.setEditable(false);
		CharsetSelector cs = new DefaultCharsetSelector("utf-8");
		String text;
		try {
			URL url = new URL("http://www.xidea.org/project/jsa/ads.html");
			text = TextResource.getText(url.openStream(), Locale.getDefault(),
					cs);//$NON-NLS-1$
			// throw new IOException();
		} catch (IOException ex) {
			text = TextResource.getText(this.getClass().getResourceAsStream(
					"ads.html"), Locale.getDefault(), cs);//$NON-NLS-1$
		}
		// System.out.println(text.substring(text.indexOf('<')).charAt(0));
		content.setText(text);
		content.setBorder(BorderFactory.createLineBorder(Color.RED));
		content.addHyperlinkListener(new HyperlinkListener() {
			public void hyperlinkUpdate(HyperlinkEvent e) {
				if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
					DesktopUtil.browse(e.getURL().toString());
				}
			}

		});
	}

}
