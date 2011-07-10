package cn.jside.jsi.tools.rhino;

import org.mozilla.javascript.CompilerEnvirons;

public class CompilerEnvironsImpl extends CompilerEnvirons {
	public CompilerEnvironsImpl() {
		setReservedKeywordAsIdentifier(true);
	}
}
