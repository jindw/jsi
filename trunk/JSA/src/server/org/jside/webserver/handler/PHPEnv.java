package org.jside.webserver.handler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jside.JSide;
import org.jside.ui.DesktopUtil;
import org.xidea.lite.impl.ParseUtil;

public class PHPEnv {
	private static Log log = LogFactory.getLog(PHPEnv.class);

	private String PHPINFO;
	private String[] PHPCMD;
	private File setupHome;
	private boolean initialized = false;
	private boolean isWin = "php.dll".equals(System.mapLibraryName("php"));

	public PHPEnv() {
		this.setupHome = new File(JSide.getHome(), "php");
	}

	public String[] getPHPCmd() {
		init();
		return PHPCMD;
	}

	public String getPHPInfo() {
		init();
		return PHPINFO;
	}

	private static void saveAndClose(InputStream in, File dest)
			throws IOException {
		FileOutputStream out = new FileOutputStream(dest);
		try {
			byte[] buf = new byte[1024];
			int c;
			while ((c = in.read(buf)) >= 0) {
				out.write(buf, 0, c);
			}
		} finally {
			out.flush();
			out.close();
			in.close();
		}
	}

	private void init() {
		if (initialized) {
			return;
		}
		String cmd = null;
		if (cmd == null) {
			cmd = findCmd("php-cgi", "php-cgi.exe");
		}
		if (cmd == null) {
			if(isWin){
				cmd = setup();
			}else{
				log.error("系统缺乏默认的PHP运行库，请手动安装，并设置PATH环境变量");
				return;
			}
		}
		String info = null;
		if (cmd != null) {
			info = run(cmd, "-v");
			if (info == null) {
				log.error("PHP 程序无法正常工作:" + cmd);
				cmd = null;
			}else{
				initCmd(cmd, info);
				log.info("PHP:"+cmd+"\n\n"+info);
				initialized = true;
				return;
			}
		}
		log.error("PHP 自动安装失败！请手动安装，并设置path变量，确保PHP可执行！");
		
	}

	private String setup() {
		String cmd = null;
		try {
			File zip = new File(setupHome, "php.zip");
			File php = new File(setupHome, "php-cgi.exe");
			if (!php.exists()) {
				log.info("安装PHP在：" + php);
				setupHome.mkdir();
				try {
					String url = "http://windows.php.net/downloads/releases/php-5.2.17-nts-Win32-VC6-x86.zip";
					url = DesktopUtil.prompt("请选择需要安装的PHP文件(只接受zip格式的自解压式安装)", url);
					File file = new File(url);
					InputStream in = file.exists()?new FileInputStream(file):new URL(url).openStream();
					saveAndClose(in, zip);
					unzip(zip, setupHome);
				} catch (Exception e) {
					throw new RuntimeException("PHP 安装失败：", e);
				}
				zip.delete();
				if (!new File(setupHome, "php.ini").exists()
						&& new File(setupHome, "php.ini-recommended").exists()) {
					new File(setupHome, "php.ini-recommended")
							.renameTo(new File(setupHome, "php.ini"));
				}
			}
			cmd = php.getAbsolutePath();
		} catch (Exception e) {
			log.warn("php 初始化失敗", e);
		}
		return cmd;
	}

	private void initCmd(String cmd, String info) {
		String[] cmds = null;
		if (cmd != null) {
			String args = "0 -d cgi.force_redirect=On";
			if (isWin) {
				File dll = new File(new File(cmd).getParentFile(),
						"ext/php_mbstring.dll");
				if (dll.exists()) {
					String result = run(cmd, "-r",
							"\"echo extension_loaded('mbstring')?'success':'';\"");
					if (result != null && !result.equals("success")) {
						args += " -d extension=ext/php_mbstring.dll";
					}
				}
			}
			String result = run(cmd, "-r",
					"\"echo ini_get('date.timezone')?'success':'';\"");
			if (result != null && !result.equals("success")) {
				args += " -d date.timezone=Asia/Chongqing";
			}
			cmds = args.split("[\\s]+");
			cmds[0] = cmd;
		}
		PHPINFO = info;
		PHPCMD = cmds;
	}

	private static String findCmd(String... cmd) {
		String path = System.getenv("PATH");
		if (path == null) {
			log.warn("未设置环境PATH变量，无法查找绝对路径");
		} else {
			String[] paths = path.split("[" + File.pathSeparatorChar + "]");
			for (String p : paths) {
				for (String n : cmd) {
					File file = new File(p, n);
					log.debug(file);
					if (file.exists()) {
						return file.getAbsolutePath();
					}
				}
			}
		}
		return null;
	}

	private static String run(String... code) {
		try {
			String cmd = code[0];
			code[0] = cmd.replaceFirst("\\-cgi(\\.exe)$", "$1");
			Process proc = Runtime.getRuntime().exec(code);
			String result = ParseUtil.loadTextAndClose(proc.getInputStream(),
					null);
			proc.destroy();
			return result.trim();
		} catch (IOException e) {
			log.error("程序运行异常:" + code, e);
			return null;
		}
	}

	private static void unzip(File zip, File destDir) throws ZipException,
			IOException {
		java.util.zip.ZipFile zin = new ZipFile(zip);
		Enumeration<? extends ZipEntry> ent = zin.entries();
		while (ent.hasMoreElements()) {
			ZipEntry ze = ent.nextElement();
			String name = ze.getName();
			try {
				if (!ze.isDirectory()) {
					InputStream in = zin.getInputStream(ze);
					File dest = new File(destDir, name);
					dest.getParentFile().mkdirs();
					saveAndClose(in,dest );
					in.close();
				}
			} catch (Exception e) {
				log.error("写入文件失败：", e);
			}
		}
		zin.close();
	}

}
