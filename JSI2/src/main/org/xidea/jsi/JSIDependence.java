package org.xidea.jsi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * 初定该实现的依赖只能时一个文件，可能时该文件的全部脚本，也可能时某个确定脚本
 * 
 * @author jindw
 * 
 */
public class JSIDependence {
	protected Map<String, List<String>> targetMap = new HashMap<String, List<String>>();
	protected final JSIPackage targetPackage;
	protected String fileName;
	protected String objectName;
	protected JSIRoot root;
	protected boolean afterLoad;

	/**
	 * @param root
	 *            JSI上下文
	 * @param sourcePackage
	 *            依赖源包
	 * @param sourceObject
	 *            依赖源元素
	 * @param targetObject
	 *            目标元素
	 * @param requiredBefore
	 *            装在前依赖
	 */
	public JSIDependence(JSIRoot root, JSIPackage targetPackage,
			String fileName, String targetObject,boolean afterLoad) {
		this.root = root;
		this.afterLoad = afterLoad;
		this.targetPackage = targetPackage;
		this.fileName = fileName;
		this.objectName = targetObject;
	}

	public boolean isAfterLoad() {
		return afterLoad;
	}

	public String getFileName() {
		return fileName;
	}

	public String getObjectName() {
		return objectName;
	}

	public void initialize(List<String> buf) {
		if (objectName instanceof String) {
			buf.add((String) objectName);
		} else {
			for (Iterator<String> it = (targetPackage.getScriptObjectMap()
					.get(fileName)).iterator(); it.hasNext();) {
				buf.add(it.next());
			}

		}
	}

	public void load(JSILoadContext loadContext) {
		if (objectName instanceof String) {
			loadContext.loadScript(this.targetPackage, fileName,
					(String) objectName);
		} else {
			loadContext.loadScript(this.targetPackage, fileName, null);
		}
	}

}
