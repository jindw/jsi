package org.jside.jsi.tools.export;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xidea.jsi.JSILoadContext;
import org.xidea.jsi.JSIPackage;
import org.xidea.jsi.ScriptLoader;
import org.xidea.jsi.impl.JSIText;

public class ExportUtil {
	private static final Log log = LogFactory.getLog(ExportUtil.class);

	static Map<String, String> writeToExported(ExportAction action) throws IOException,
			FileNotFoundException {
		final Map<String, String> exportResultMap = action.requiredExportData();
		String exportedText = JSAExportorAdaptor.join(exportResultMap,
				action.getLineSeparator());
		exportedText = action.compressText(exportedText);
		ExportText.write(exportedText, new FileOutputStream(action.getOutputExported()),
				action.getEncoding());

		log.info("=========================");
		log.info("成功导出文件：" + action.getOutputExported());
		return exportResultMap;
	}

	static void writeToBoot(JSILoadContext bootCachedContext,ExportAction action) throws FileNotFoundException, IOException {
		List<ScriptLoader> list = bootCachedContext.getScriptList();
		Map<String, String> exportMap = bootCachedContext.getExportMap();
		Map<String, String> bootCachedResultMap = new JSAExportorAdaptor(
				action.getConfig(), action.getInternalPrefix(), action.isPreserve()).export(list, exportMap);
		String bootData = action.getCacheText(null, "boot.js");
		File outputBoot = action.getOutputBoot();
		Map<String, Map<String, String>> packageSourceMap = toPackageSourceMap(action,bootCachedResultMap);

		if (!packageSourceMap.isEmpty()) {
			PackageInfo packageInfo = new PackageInfo(action, action.getAllImported(), true);
			for (String pkgName : packageInfo.getPackageSet()) {
				if (!packageSourceMap.containsKey(pkgName)) {
					HashMap<String, String> fileMap = new HashMap<String, String>();
					packageSourceMap.put(pkgName, fileMap);
					fileMap.put(JSIPackage.PACKAGE_FILE_NAME, action.getCacheText(pkgName,
							JSIPackage.PACKAGE_FILE_NAME));
				}
			}
			for (String pkgName : packageInfo.getValidPackageSet()) {
				if (!packageSourceMap.containsKey(pkgName)) {
					packageSourceMap.put(pkgName, null);
				}
			}

			String prefix = ExportText.findDocumentHeader(bootData);
			// {});
			StringBuilder buf = new StringBuilder(bootData.substring(0,
					bootData.lastIndexOf('{') + 1));
			for (String pkgName : packageSourceMap.keySet()) {
				Map<String, String> fileMap = packageSourceMap.get(pkgName);
				buf.append("'");
				buf.append(pkgName);
				buf.append("':");
				buf.append(buildPreloadData(fileMap, prefix));
				buf.append(",");// delete last
			}
			buf.delete(buf.length() - 1, buf.length());
			buf.append("})");
			bootData = buf.toString();
		}
		ExportText.write(action.compressText(bootData), new FileOutputStream(
				outputBoot), action.getEncoding());

		log.info("=========================");
		log.info("成功导出JSI引导文件（boot.js）至：" + outputBoot);
	}

	static void wirteToPackage(JSILoadContext exportContext,ExportAction action) throws FileNotFoundException, IOException {

		File outputExported = action.getOutputExported();
		File outputPackage = action.getOutputPackage();
		String encoding = action.getEncoding();
		StringBuilder buf = new StringBuilder("this.addScript('");
		if (outputExported != null) {
			buf.append(outputExported.getName());
		} else {
			buf.append("<file>.js");
		}
		buf.append("',[");
		for (String var : exportContext.getExportMap().keySet()) {
			buf.append("'");
			buf.append(var);
			buf.append("',");
		}
		buf.setCharAt(buf.length() - 1, ']');
		String dependence = new DependenceFinder(action)
				.find(action.getExports(), action.getBootCached());

		if (dependence != null) {
			buf.append(",");
			buf.append(dependence);
		}
		buf.append(")");
		ExportText.write(buf.toString(), new FileOutputStream(outputPackage),
				encoding);

		log.info("=========================");
		log.info("成功导出JSI包文件至：" + outputPackage);

	}

	static void wirteToPreload(Map<String, String> fileMap,ExportAction action)
			throws FileNotFoundException, IOException {
		if (fileMap.isEmpty()) {
			log.warn("需要输出的文件为空");
			return;
		}
		String prefix = ExportText.findDocumentHeader(fileMap.values()
				.iterator().next());
		Map<String, Map<String, String>> packageSourceMap = toPackageSourceMap(action,fileMap);
		StringBuilder buf = new StringBuilder(prefix);
		for (String pkgName : packageSourceMap.keySet()) {
			buf.append(JSIText.PRELOAD_PREFIX);
			buf.append("'" + pkgName + "',");
			buf.append(buildPreloadData(packageSourceMap.get(pkgName), prefix));
			buf.append(");");
		}

		ExportText.write(action.compressText(buf.toString()), new FileOutputStream(
				action.getOutputPreload()), action.getEncoding());

		log.info("=========================");
		log.info("成功导出预装载文件至：" + action.getOutputPreload());

	}


	static void wirteToJARResult(Map<String, String> fileMap,ExportAction action)
			throws IOException {
		if (fileMap.isEmpty()) {
			log.warn("需要输出的文件为空");
			return;
		}

		File outputJAR = action.getOutputJAR();
		String encoding = action.getEncoding();
		JarOutputStream out = new JarOutputStream(new FileOutputStream(
				outputJAR));
		String prefix = "";
		for (String path : fileMap.keySet()) {
			String content = fileMap.get(path);
			JarEntry entry = new JarEntry(path);
			out.putNextEntry(entry);
			if (content == null) {
				log.warn("指定路径无有效数据：" + path);
			}
			out.write(content.getBytes(encoding));
			out.closeEntry();
			if (!path.endsWith(ExportText.PACKAGE_PATH_POSTFIX)) {
				// 注释提前
				if (prefix.length() > 0 && content.startsWith(prefix)) {
					content = content.substring(prefix.length());
				} else {
					prefix = ExportText.findDocumentHeader(content);
					content = content.substring(prefix.length());
				}
				entry = new JarEntry(path.replaceFirst("\\.js$",
						JSIText.PRELOAD_FILE_POSTFIX));
				out.putNextEntry(entry);
				out.write(prefix.getBytes(encoding));
				out.write(JSIText.buildPreloadPerfix(path).getBytes(encoding));
				out.write(content.getBytes(encoding));
				out.write(JSIText.buildPreloadPostfix(content).getBytes(
						encoding));
				out.closeEntry();
			}
		}
		out.flush();
		out.close();

		log.info("=========================");
		log.info("成功导出JAR至：" + outputJAR);
	}

	protected static Map<String, Map<String, String>> toPackageSourceMap(ExportAction action,
			Map<String, String> resultMap) {
		Map<String, Map<String, String>> sourceMap = new HashMap<String, Map<String, String>>();
		for (String path : resultMap.keySet()) {
			String pkgName = ExportText.toPackageName(path);
			Map<String, String> fileMap = sourceMap.get(pkgName);
			if (fileMap == null) {
				fileMap = new HashMap<String, String>();
				sourceMap.put(pkgName, fileMap);
				String packagePath = ExportText.toPackagePath(path);
				fileMap.put(JSIPackage.PACKAGE_FILE_NAME, action.getCacheText(null,
						packagePath));
			}
			String name = path.substring(pkgName.length() + 1);
			fileMap.put(name, resultMap.get(path));

		}
		return sourceMap;
	}
	private static String buildPreloadData(Map<String, String> fileMap, String prefix) {
		if (fileMap == null) {
			return "null";
		}
		StringBuilder buf = new StringBuilder();
		buf.append("{");
		for (String fileName : fileMap.keySet()) {
			String content = fileMap.get(fileName);
			if (prefix.length() > 0 && content.startsWith(prefix)) {
				content = content.substring(prefix.length());
			} else {
				prefix = ExportText.findDocumentHeader(content);
				content = content.substring(prefix.length());
			}
			buf.append("'");
			if (JSIPackage.PACKAGE_FILE_NAME.equals(fileName)) {
				buf.append("':");
				buf.append("function(){");
			} else {
				buf.append(fileName);
				buf.append("':");
				buf.append("function(){");
				buf.append(JSIText.PRELOAD_CONTENT_PREFIX);
			}
			buf.append(content);
			buf.append("},");
		}
		buf.delete(buf.length() - 1, buf.length());
		buf.append("}");
		return buf.toString();
	}

	// utils
}
