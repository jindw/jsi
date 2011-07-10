package org.jside;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.swing.JOptionPane;

public class JSideImpl {
	private List<JSideListener<? extends Object>> eventMap = new ArrayList<JSideListener<? extends Object>>();
	private String home;
	private File commonConfigFile;
	private Properties commonConfig = new Properties();

	protected void initializeIO() {
		try {
			String configedHome = System.getProperty("jside.home");
			if (configedHome == null) {
				home = new File(System.getProperty("user.home"), ".JSIDE")
						.getCanonicalFile().getAbsolutePath();
				System.setProperty("jside.home", home);
			} else {
				home = configedHome;
			}
			commonConfigFile = new File(home, "common.config");
			if (!new File(home).exists()) {
				new File(home).mkdir();
			}
			if (commonConfigFile.exists()) {
				commonConfig.load(new FileInputStream(commonConfigFile));
			} else {
				commonConfigFile.createNewFile();
			}
		} catch (IOException e) {
		}

	}

	public JSideImpl() {
	}

	public String getAttribute(String key) {
		return commonConfig.getProperty(key);
	}

	public void setAttribute(String key, String value) {
		commonConfig.setProperty(key, value);
		try {
			commonConfig.store(new FileOutputStream(commonConfigFile),
					"JSide Globals Config");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	public <T> T loadConfig(File file, Class<T> cls) {
		try {
			if (file.exists() && file.isFile()) {
				InputStream in = new FileInputStream(file);
				if (in != null) {
					in = new BufferedInputStream(in, 1);
					in.mark(1);
					int first = in.read();
					if (first == 0xFF) {
						in.read();
					} else {
						in.reset();
					}
					XMLDecoder decoder = new XMLDecoder(in);
					return (T) decoder.readObject();
				}
			}
			if (cls != null) {
				return cls.newInstance();
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		return null;
	}

	public <T> void saveConfig(File file, T object) {
		try {
			if (!file.exists() || !file.isFile()) {
				file.createNewFile();

			}
			FileOutputStream out = new FileOutputStream(file);

			XMLEncoder encoder = new XMLEncoder(out);
			encoder.writeObject(object);
			encoder.flush();
			encoder.close();
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public void initialize(String[] plugins) {
		try {
			initializeIO();
			initializePlugins(plugins);
		} catch (Throwable e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "错误信息：" + e.getMessage()
					+ "\n程序将自动退出！", "启动失败", JOptionPane.ERROR_MESSAGE);

			System.exit(1);
		}
	}

	protected void initializePlugins(String[] plugins)
			throws NoSuchMethodException, ClassNotFoundException,
			IllegalAccessException, InvocationTargetException {
		for (String arg : plugins) {
			String[] args2 = arg.split("[\\s]+");
			String[] args3 = new String[args2.length - 1];
			System.arraycopy(args2, 0, args2, 0, args2.length);
			Method method = Class.forName(args2[0]).getMethod("main",
					String[].class);
			method.invoke(null, (Object) args3);
		}
	}

	public void addListener(JSideListener<? extends Object> listener) {
		synchronized (eventMap) {
			eventMap.add(listener);
		}
	}

	@SuppressWarnings("unchecked")
	public boolean fireEvent(Class<? extends JSideListener<? extends Object>> type, Object source) {
		synchronized (eventMap) {
			for (JSideListener listener : eventMap) {
				try {
					if (type.isInstance(listener) && listener.execute(source)) {
						return true;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return false;
	}

	public List<JSideListener<? extends Object>> removeAllListener(
			Class<? extends JSideListener<? extends Object>> type) {
		ArrayList<JSideListener<? extends Object>> result = new ArrayList<JSideListener<? extends Object>>();
		synchronized (eventMap) {
			for (Iterator<JSideListener<? extends Object>> it = eventMap.iterator(); it.hasNext();) {
				JSideListener<? extends Object> listener = it.next();
				if (type.isInstance(listener)) {it.remove();
					result.add(listener);
				}
			}
		}
		return result;
	}

	public String getHome() {
		return home;
	}
}
