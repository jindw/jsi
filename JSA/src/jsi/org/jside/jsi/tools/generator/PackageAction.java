package org.jside.jsi.tools.generator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.xidea.jsi.impl.ResourceRoot;
import org.xidea.el.impl.CommandParser;
import org.xidea.el.json.JSONEncoder;

public class PackageAction extends ResourceRoot{
	private String packageName;
	private String result;
	private File output;
	private ResourceRoot root = this;
	private Map<String, Boolean> packageMap;
	private File scriptBase;
	public PackageAction(ResourceRoot root) {
		this.root = root;
	}
	public PackageAction() {
	}

	public static void main(String[] args) throws IOException {
		System.out.println("Args:" + JSONEncoder.encode(args));
		// args = new
		// String[]{"-scriptBase","D:\\workspace\\JSI2/web/scripts","-output","D:\\workspace\\JSI2/build/dest/package-gen.js","-packageName","org.xidea.jsidoc","-dependences","org.xidea.jsidoc.export","org.xidea.jsidoc.util"};

		PackageAction action = new PackageAction();
		CommandParser.setup(action, args);
		action.execute();
		String result = action.getResult();
		if (result == null) {
			System.out
					.println("必须制定：packageName属性,eg：-packageName com.mypackage");
		} else if (action.output != null) {
			System.out.println("自动包定义信息生成在："+action.output);
			FileOutputStream out = new FileOutputStream(action.output);
			String encoding = action.getEncoding();
			if (encoding == null) {
				encoding = "utf-8";
			}
			out.write(result.getBytes(encoding));
			out.flush();
			out.close();
		} else {
			System.out.println(action.result);
		}

	}

	public String execute() {
		JSIPackageGenerator gen = new JSIPackageGenerator(root);
		List<String> sourcePackages = root.findPackageList(false);
		List<String> allPackages = root.findPackageList(true);
		
		packageMap = JSIPackageGeneratorUnits.getPackageFileExistMap(scriptBase);

		if (packageName != null) {
			allPackages.removeAll(sourcePackages);
			sourcePackages.remove(packageName);
			List<String> packages = new ArrayList<String>();
			packages.add(packageName);
			packages.addAll(sourcePackages);
			packages.addAll(allPackages);
			
			gen.addPackageDependanceList(packageName, packages.toArray(new String[packages.size()]));
			this.result = gen.genPackageSource(packageName);
		}
		return "package.xhtml";
	}

	public void setSources(File[] sources) {
		this.scriptBase = sources[0];
		for(File source:sources){
			super.addSource(source);
		}
	}
	public void setLibs(File[] libs) {
		for(File lib:libs){
			super.addLib(lib);
		}
	}
	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}


	public Map<String, Boolean> getPackageMap() {
		return packageMap;
	}

	public String getResult() {
		return result;
	}

	public void setOutput(File output) {
		this.output = output;
	}
}
