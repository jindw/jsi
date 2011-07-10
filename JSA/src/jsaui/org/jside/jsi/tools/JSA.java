package org.jside.jsi.tools;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultTreeModel;

import org.jside.JSide;
import org.jside.JSideListener;
import org.jside.JSideWebServer;
import org.jside.jsi.tools.ui.JSAConfig;
import org.jside.jsi.tools.ui.frame.JSAFrame;
import org.jside.jsi.tools.ui.frame.project.ProjectRootNode;
import org.jside.jsi.tools.ui.frame.project.ProjectTree;
import org.jside.jsi.tools.web.ScriptAction;
import org.jside.ui.ContextMenu;
import org.jside.ui.DockUI;

public class JSA {
	public static final String HOST = "jsa.jside.org";
	public static JavaScriptCompressor compressor = JSAToolkit.getInstance().createJavaScriptCompressor();
	static{
		compressor.setCompressorConfig(JSAConfig.getInstance());
	}

	public static JavaScriptCompressor getCompressor() {
		return compressor;
	}
	private static ActionListener openUIAction = new ActionListener(){
		public void actionPerformed(ActionEvent e) {
			new Thread() {
				public void run() {
					long t2 = System.currentTimeMillis();
					JSAFrame.showUI();
					System.out.println("启动UI耗时:"
							+ (System.currentTimeMillis() - t2));
				}
			}.start();
		}
	};
		

	@SuppressWarnings("unchecked")
	static JSideListener webRootChanged =new JSideListener.WebRootChange(){
		public boolean execute(File source) {
			File file = (File)source;
			List<String> projectList = new ArrayList<String>();
			projectList.add(file.getAbsolutePath());
			JSAConfig ac = JSAConfig.getInstance();
			ac.setProjectList(projectList);
			ac.save();
			ProjectTree.getInstance().setModel(
					new DefaultTreeModel(new ProjectRootNode()));
			return false;
		}
		
	};

	@SuppressWarnings("unchecked")
	private static JSideListener beforeOpenAction = new JSideListener.BeforeFileOpen() {
		public boolean execute(File file) {
			if (file.isFile() && file.getName().toLowerCase().endsWith(".js")) { //$NON-NLS-1$
				DockUI.getInstance().updateMessage("压缩脚本");
				return true;
			} else {
				return false;
			}
		}
	};
	@SuppressWarnings("unchecked")
	private static JSideListener openAction = new JSideListener.FileOpen() {
		public boolean execute(File source) {
			File file = (File) source;
			if (file.isFile() && file.getName().toLowerCase().endsWith(".js")) { //$NON-NLS-1$

				DockUI.getInstance().updateMessage("准备压缩:", "将在默认浏览器操作",
						file.getName());
				JSAFrame.getInstance().openFile(file);
				JSAFrame.showUI();
				return true;
			} else {
				return false;
			}
		}
	};
	/**
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public static void main(String args[]) throws Exception {
		ContextMenu cm = ContextMenu.getInstance();
		cm.addMenuSeparator();
		//cm.addMenuItem("JSA工具首页",null,WebLinkAction.createScriptLink("tools.xhtml"));
		cm.addMenuItem("打开JSA窗口",null,openUIAction);
		
		JSideWebServer server = JSideWebServer.getInstance();
		String scriptBase = "/scripts/";
		server.getApplication().put(JSideWebServer.SCRIPT_BASE_KEY,
				scriptBase);
		server.addAction(scriptBase + "**", ScriptAction.class);
		JSFormatFilter jsFormator = new JSFormatFilter();
		//ProxyHandler.getInstance().addResponseContentFilter("**.js", jsFormator);
		
		JSide.addListener(webRootChanged);
		JSide.addListener(new JSideListener.DockClick(){
			public boolean execute(MouseEvent e) {
				if(e.getClickCount() >=2){
					openUIAction.actionPerformed(null);
				}
				return false;
			}

		} );
		JSide.addListener( beforeOpenAction);
		JSide.addListener(openAction);
	}

}
