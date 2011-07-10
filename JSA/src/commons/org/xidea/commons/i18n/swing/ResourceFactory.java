/*
 * Created on 2004-10-22
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.xidea.commons.i18n.swing;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

/**
 * 国际化资源工厂 国际化资源以接口（或类）的Class对象为键值缓存起来
 * 
 * @project fix
 * @author 金大为
 */
class ResourceFactory {
    private Map resourceCache = new HashMap();

    public Object getResourceBundle(Class cls) {
        return getResourceBundle(cls, Locale.getDefault());
    }

    public Object getResourceBundle(final Class cls, final Locale locale) {
        Object resource = resourceCache.get(cls);
        if (resource != null) {
            return resource;
        }

        if (cls.isInterface()) {//生成资源代理

            InvocationHandler handler = new ResourceInvocationHandler(cls,
                    locale);
            Class proxyClass = Proxy.getProxyClass(cls.getClassLoader(),
                    new Class[] { cls });
            try {
                //获取代理实例
                resource = proxyClass.getConstructor(
                        new Class[] { InvocationHandler.class }).newInstance(
                        new Object[] { handler });
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            try {
                //获取实例
                resource = cls.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (resource == null) {
            resourceCache.put(cls, resource);
        }
        return resource;
    }

    Object getResource(Object key) {
        return null;
    }

    public Map getResource(Class cls, Locale lc) {
        String name = cls.getName();
        String resourceKey = name + lc;
        Map res = (Map) resourceCache.get(resourceKey);
        if (res != null) {
            return res;
        }
        res = new HashMap();

        int split = name.lastIndexOf('.') + 1;

        String localeKey = name + '_' + lc + ".properties";

        String languageKey = name + '_' + lc.getLanguage() + ".properties";

        String rootKey = name + ".properties";

        Properties rootProps = (Properties) resourceCache.get(rootKey);
        if (rootProps == null) {
            InputStream in = cls.getResourceAsStream(rootKey.substring(split));
            if (in != null) {
                rootProps = new Properties();
                try {
                    rootProps.load(in);
                    resourceCache.put(rootKey, rootProps);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        }
        fillResource(rootProps, res);

        Properties languageProps = (Properties) resourceCache.get(languageKey);
        if (languageProps == null) {
            InputStream in = cls.getResourceAsStream(languageKey
                    .substring(split));
            if (in != null) {
                languageProps = new Properties();
                try {
                    languageProps.load(in);
                    resourceCache.put(languageKey, languageProps);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        }
        fillResource(languageProps, res);

        Properties localeProps = (Properties) resourceCache.get(localeKey);
        if (localeProps == null) {
            InputStream in = cls
                    .getResourceAsStream(localeKey.substring(split));
            if (in != null) {
                localeProps = new Properties();
                try {
                    localeProps.load(in);
                    resourceCache.put(localeKey, localeProps);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        }
        fillResource(localeProps, res);
        return res;
    }

    protected static void fillResource(Map from, Map to) {
        if (from != null && to != null) {
            Iterator it = from.keySet().iterator();
            while (it.hasNext()) {
                Object key = it.next();
                if (key instanceof String) {
                    to.put("$" + key + "$", from.get(key));
                    //System.out.println(key+"/"+from.get(key));
                }
            }
        }
    }

    class ResourceInvocationHandler implements InvocationHandler {
        Class cls = null;
        Locale locale = null;
        Map resource = null;

        ResourceInvocationHandler(Class cls, Locale locale) {
            this.cls = cls;
            this.locale = locale;
            resource = getResource(cls, locale);
        }

        public Object invoke(Object proxy, Method method, Object[] args)
                throws Throwable {
            String name = method.getName();
            if (args == null) {
                if (name.charAt(0) == '$'
                        && name.charAt(name.length() - 1) == '$') {
                    Object obj = resource.get(name);
                    if (obj != null) {
                        return obj.toString();
                    }
                } else if (name.equals("setLocale") && args != null
                        && args.length == 0 && args[0] instanceof Locale) {
                    this.resource = getResource(cls, (Locale) args[0]);

                }

            }
            return name;
        }
    };
}