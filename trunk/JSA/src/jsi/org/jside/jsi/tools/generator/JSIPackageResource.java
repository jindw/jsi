package org.jside.jsi.tools.generator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jside.jsi.tools.JSAToolkit;
import org.jside.jsi.tools.JavaScriptAnalysisResult;
import org.jside.jsi.tools.JavaScriptCompressor;
import org.jside.jsi.tools.util.JavaScriptConstants;
import org.xidea.jsi.JSIDependence;
import org.xidea.jsi.impl.DefaultPackage;

public class JSIPackageResource extends DefaultPackage {
	private static final Log log = LogFactory.getLog(JSIPackageResource.class);
	public static JavaScriptCompressor compressor = JSAToolkit.getInstance()
			.createJavaScriptCompressor();
	// protected final Map<String, JavaScriptAnalysisResult> analysisResultMap =
	// new HashMap<String, JavaScriptAnalysisResult>();
	protected final Map<String, String> sourceMap = new HashMap<String, String>();

	private JSIPackageGenerator root;
	private boolean initialized = false;

	Map<String, List<JSIDependence>> dependenceMap2 = new HashMap<String, List<JSIDependence>>();

	public JSIPackageResource(JSIPackageGenerator root, String name) {
		super(root, name);
		this.root = root;
	}

	public void initialize() {
		if (!initialized) {
			initialized = true;
			super.initialize();
			Map<String, List<String>> som = this.getScriptObjectMap();
			for (String scriptName : som.keySet()) {
				String source = this.loadText(scriptName);
				if(source == null){
					log.error("File not found:"+scriptName);
					continue;
				}
				JavaScriptAnalysisResult result = compressor.analyse(source);
				Collection<String> topExternalVars = new HashSet<String>(result
						.getTopExternalVars());
				Collection<String> externalVars = new HashSet<String>(result
						.getExternalVars());

				topExternalVars.removeAll(JavaScriptConstants.ALL_VARIBALES);
				externalVars.removeAll(JavaScriptConstants.ALL_VARIBALES);
				externalVars.removeAll(topExternalVars);

				List<JSIDependence> list = this.root.findDependence(this,
						topExternalVars, false);
				this.putDependence(scriptName, list);
				list = this.root.findDependence(this, externalVars, true);
				this.putDependence(scriptName, list);
			}
		}
	}

	private void putDependence(String thisFile,
			List<JSIDependence> dependenceList) {
		if (dependenceList != null ) {
			List<JSIDependence> list = dependenceMap2.get(thisFile);
			if(list!=null){
				list = new ArrayList<JSIDependence>(list);
				list.addAll(dependenceList);
				dependenceList= list;
			}
			dependenceMap2.put(thisFile, dependenceList);
		}
	}

	@Override
	public Map<String, List<JSIDependence>> getDependenceMap() {
		return this.dependenceMap2;
	}

}
