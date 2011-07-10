package org.jside.jsi.tools.export;

import java.util.Map;

import org.xidea.el.impl.CommandParser;
import org.xidea.jsi.JSIExportor;
import org.xidea.jsi.impl.DefaultExportorFactory;

public class JSAExportorFactory extends DefaultExportorFactory {
	public JSAExportorFactory() {
	}
	private JSIExportor createJSAExplorter(Map<String, String[]> params,boolean preserve) {
		JSAExportorAdaptor exportor = new JSAExportorAdaptor(); 
		CommandParser cp = new CommandParser(null);
		cp.setup(exportor,params);
		exportor.setPreserve(preserve);
		return exportor;
	}
	@Override
	protected JSIExportor createConfuseExplorter(Map<String, String[]> params) {
		return createJSAExplorter(params,false);
	}

	@Override
	protected JSIExportor createReserveExplorter(Map<String, String[]> params) {
		return createJSAExplorter( params,true);
	}
	@Override
	public JSIExportor createReportExplorter(Map<String, String[]> params) {
		return new JSAReportExplorter();
	}


}

