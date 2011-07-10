package org.xidea.commons.i18n.swing;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.UIManager;

import org.jside.jsi.tools.ui.Messages;

@SuppressWarnings("unchecked")
public class MessageBase {
	protected static Map<Class, MessageConfig> configMap = new HashMap<Class, MessageConfig>();

	public static JMenu buildMenu(String label, final Class resourceClass,
			final Component[] components, final Locale[] locales) {
		initialize(Messages.class, locales[0]);
		JMenu menu = new JMenu(label);
		ButtonGroup group = new ButtonGroup();
		int length = 0;
		String dlc = Locale.getDefault().toString();
		for (int i = 0; i < locales.length; i++) {
			String lc = locales[i].toString();
			boolean checked = false;
			if (dlc.indexOf(lc) == 0) {
				if (lc.length() > length) {
					checked = true;
				}
			}

			final Locale switchLocale = locales[i];
			JCheckBoxMenuItem item = new JCheckBoxMenuItem(switchLocale
					.getDisplayName(locales[i]), checked);
			item.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					switchLocale(resourceClass, switchLocale, components);
				}
			});
			group.add(item);
			menu.add(item);
		}
		return menu;
	}

	public static Icon loadIcon(Class<? extends Object> clazz, String... paths) {
		for (String path : paths) {
			try {
				Icon icon = UIManager.getIcon(path);
				if (icon == null) {
					icon = new ImageIcon(clazz.getResource(path));
				}
				if (icon != null) {
					return icon;
				}
			} catch (Exception e) {
			}
		}
		return null;
	}

	public static Map getResource(Class cls, Locale locale, String sourceLocal) {
		// System.out.println(lc);
		MessageConfig config = configMap.get(cls);
		Map<String, String> res = config.resourceCache.get(locale.toString());
		if (res != null) {
			return res;
		}
		String localeString = locale.toString();
		String flag = localeString;
		// System.out.println(Arrays.asList(flags));
		Map<String, String> map = null;
		InputStream in = null;
		String simpleName = cls.getName();
		simpleName = simpleName.substring(simpleName.lastIndexOf('.') + 1);
		do {
			in = cls.getResourceAsStream(simpleName
					+ (flag.length() > 0 ? '_' + flag : "") + ".properties");
			if (in == null) {
				if ((map = config.resourceCache.get(flag)) != null) {
					break;
				} else if (flag.equals(sourceLocal)) {
					map = toMap(cls);
					// System.out.println(map);
					break;
				}
			} else {
				Properties properties = new Properties();
				map = new HashMap<String, String>();
				if (in != null) {
					try {
						properties.load(in);
					} catch (IOException e) {
						e.printStackTrace();
					}
					for (Object key : properties.keySet()) {
						map.put((String) key, properties
								.getProperty((String) key));
					}

				}
				break;
			}
			if (flag.length() > 0) {
				flag = flag.substring(0, Math.max(flag.lastIndexOf('_'), 0));
			} else {
				break;
			}
		} while (true);
		int i = flag.length();
		do {
			config.resourceCache.put(localeString.substring(0, i), map);
			i = localeString.indexOf('_', i + 1);
		} while (i > 0);
		return map;
	}

	protected static void initialize(Class clazz, Locale sourceLocale) {
		if (configMap.containsKey(clazz)) {
			return;
		}
		MessageConfig config = new MessageConfig();
		configMap.put(clazz, config);
		Map<String, String> input = getResource(clazz, Locale.getDefault(),
				sourceLocale.toString());
		exchangeMap("", clazz,  input, null);
	}

	private static Map<String, String> toMap(Class clazz) {
		Map<String, String> output = new HashMap<String, String>();
		exchangeMap("", clazz,  null, output);

		return output;
	}

	private static void exchangeMap(String prefix, Class clazz, 
			final Map<String, String> input, final Map<String, String> output) {
		for (Field field : clazz.getFields()) {
			String key = field.getName();
			try {
				Object value = field.get(null);
				if (field.getType() == String.class) {
					String name = prefix + key;
					if (output != null && value != null) {
						output.put(name, (String) value);
					}
					if (input != null && (value = input.get(name)) != null) {
						field.set(null, value);
					}
				} else if (value != null) {
					//
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		Class[] classes = clazz.getDeclaredClasses();
		for (Class clazz2 : classes) {
			exchangeMap(clazz2.getSimpleName()+'.', clazz2, input, output);
		}
	}

	public static void switchLocale(Class clazz, Locale switchLocale,
			Component[] components) {

		Map<String, String> backupMap = new HashMap<String, String>();
		Map<String, String> input = getResource(clazz, switchLocale, null);
		exchangeMap("", clazz, input, backupMap);
		Map<String, String> replace = new HashMap<String, String>();
		//System.out.println(input);
		//System.out.println(backupMap);
		for (Iterator it = backupMap.keySet().iterator(); it.hasNext();) {
			String key = (String) it.next();

			// System.out.println(key == null);
			// System.out.println(input.get(key) == null);
			replace.put(backupMap.get(key), input.get(key));
		}
		SwingTextUpdater swingTextUpdater = new SwingTextUpdater();
		// System.out.println(replace);
		for (int i = 0; i < components.length; i++) {
			swingTextUpdater.update(replace, components[i]);
		}
	}

}

class MessageConfig {

	Map<String, Map<String, String>> resourceCache = new HashMap<String, Map<String, String>>();

	// Map<String, String> backupMap;

}
