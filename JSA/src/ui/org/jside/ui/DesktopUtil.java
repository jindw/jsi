package org.jside.ui;

import java.awt.Shape;
import java.awt.Window;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;

public class DesktopUtil {
	private static Method setWindowOpacity;
	private static Method setWindowOpaque;
	private static Method setWindowShape;
	static {
		try {
			Class<?> clazz = Class.forName("com.sun.awt.AWTUtilities");
			setWindowOpacity = clazz.getMethod("setWindowOpacity",
					Window.class, Float.TYPE);
			setWindowOpaque = clazz.getMethod("setWindowOpaque", Window.class,
					Boolean.TYPE);
			setWindowShape = clazz.getMethod("setWindowShape", Window.class,
					Shape.class);
		} catch (Exception e) {
			// String jv = System.getProperty("java.class.version");
			new Thread() {
				public void run() {
					try {
						Thread.sleep(1000 * 2);
					} catch (InterruptedException e) {
					}
					alert("建议使用java6u10以上版本jvm。\n您当前使用得jvm版本为:"
							+ System.getProperty("java.version"));
				}
			}.start();
		}
	}

	public static void setOpacity(Window window, double opacity) {
		invokeStatic(setWindowOpacity, window, (float) opacity);
	}

	public static void setOpacity(final Window window, final double... opacitys) {
		new OpacityThread(window, opacitys).start();
	}

	private static class OpacityThread extends Thread {
		private Window window;
		private double[] opacitys;
		private static int inc = 0;

		OpacityThread(Window window, double... opacitys) {
			this.window = window;
			this.opacitys = opacitys;
			inc++;
		}

		public void run() {
			try {
				int previousInterval = 0;
				double previousOpacity = 0;
				final int currentInc = inc;
				for (int i = 0; i < opacitys.length; i++) {
					double opacity = opacitys[i];
					final int interval = (int) opacity;
					final double newOpacity;
					if (i + 1 == opacitys.length) {
						newOpacity = opacity;
					} else {
						newOpacity = opacity - interval;
					}
					int step = previousInterval / 25;
					double stepOpacity = (newOpacity - previousOpacity) / step;
					while (step > 0) {
						if (currentInc != inc) {
							return;
						}
						setOpacity(window, previousOpacity);
						previousOpacity += stepOpacity;
						Thread.sleep(25);
						step--;
					}
					if (currentInc != inc) {
						return;
					}
					setOpacity(window, newOpacity);
					previousInterval = interval;
					previousOpacity = newOpacity;

				}
			} catch (Exception e) {
			}

		}
	}

	public static void setOpaque(Window window, boolean opaque) {
		invokeStatic(setWindowOpaque, window, opaque);
	}

	public static void setShape(Window window, Shape shape) {
		invokeStatic(setWindowShape, window, shape);
	}

	private static void invokeStatic(Method method, Object... args) {
		if (method != null) {
			try {
				method.invoke(null, args);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static boolean browse(String uri) {
		try {
			java.awt.Desktop.getDesktop().browse(new URI(uri));
			return true;
		} catch (Throwable ex) {
		}
		try {
			Runtime.getRuntime().exec("explorer " + uri);
			return true;
		} catch (IOException ex) {
			ex.printStackTrace();
			return false;
		}
	}

	public static File openFileDialog(java.lang.String pathHint,
			java.lang.String... extensions) throws java.io.IOException {
		JFileChooser fileChooser = new JFileChooser();
		ExtensionsFileFilter filter = new ExtensionsFileFilter(extensions);
		fileChooser.setFileFilter(filter);
		fileChooser.setSelectedFile(new File(pathHint));
		int state = fileChooser.showOpenDialog(null);
		if (state == JFileChooser.APPROVE_OPTION) {
			return fileChooser.getSelectedFile();
		}
		return null;

		// throw new java.lang.UnsupportedOperationException("Method
		// getOutputStream() not yet implemented.");
	}

	public static File saveFileDialog(java.io.InputStream in,
			java.lang.String name, java.lang.String pathHint,
			java.lang.String... extensions) throws java.io.IOException {
		JFileChooser fileChooser = new JFileChooser();
		ExtensionsFileFilter filter = new ExtensionsFileFilter(extensions);
		fileChooser.setFileFilter(filter);
		fileChooser.setSelectedFile(new File(pathHint));
		int state = fileChooser.showSaveDialog(null);

		if (state == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			file.createNewFile();
			FileOutputStream fo = new FileOutputStream(file);
			int b = in.read();
			while (b > -1) {
				fo.write(b);
				b = in.read();
			}
			return file;
		}
		return null;

		// throw new java.lang.UnsupportedOperationException("Method
		// getOutputStream() not yet implemented.");
	}

	public static void alert(Object message) {
		JOptionPane.showMessageDialog(DockUI.getInstance(), message, UIManager
				.getString("OptionPane.messageDialogTitle"),
				JOptionPane.OK_OPTION);
	}

	public static boolean confirm(Object message) {
		int result = JOptionPane.showConfirmDialog(DockUI.getInstance(),
				message, UIManager.getString("OptionPane.titleText"),
				JOptionPane.YES_NO_OPTION);
		return JOptionPane.OK_OPTION == result;
	}

	public static String prompt(Object message, Object value) {
		return JOptionPane
				.showInputDialog(DockUI.getInstance(), message, value);
	}

	public static void explorer(File file) {
		try {
			browse(file.toURI().toURL().toString());
		} catch (MalformedURLException e) {
			alert(e);
		}
	}

}

class ExtensionsFileFilter extends FileFilter {
	String[] extensions;

	public ExtensionsFileFilter(String[] exts) {
		//
		extensions = new String[exts.length];
		for (int i = 0; i < extensions.length; i++) {
			extensions[i] = "." + exts[i].toLowerCase();
		}
	}

	public boolean accept(File f) {
		if (f.isDirectory()) {
			return true;
		}
		if (extensions.length > 0) {
			for (int i = 0; i < extensions.length; i++) {
				String p = f.getName().toLowerCase();
				if (p.endsWith(extensions[i])) {
					return true;
				}
				// filter.setDescription("");
			}
			return false;
		} else {
			return true;
		}
	}

	public String getDescription() {
		return null;
	}

}
