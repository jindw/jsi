package org.xidea.jsi.impl.v3;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mozilla.javascript.Parser;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.AstRoot;
import org.mozilla.javascript.ast.FunctionCall;
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.NodeVisitor;
import org.mozilla.javascript.ast.StringLiteral;
import org.xidea.jsi.JSIRoot;
import org.xidea.jsi.JSIRuntime;
import org.xidea.jsi.util.CompilerEnvironsImpl;

public class ScriptDocument {
	private static final Log log = LogFactory.getLog(RuntimeSupport.class);
	private JSIRuntime runtime;

	private ScriptDocument(JSIRuntime runtime) {
		this.runtime = runtime;
	}

	// document.scripts
	// document.write();
	public static void bind(JSIRuntime runtime) {
		ScriptDocument document = new ScriptDocument(runtime);
		Object ini = runtime
				.eval("var document;(function(impl){document=impl})");
		runtime.invoke(null, ini, document);
	}

	public void write(String html) {
		if (html.startsWith("<script")) {
			String src = html.replaceFirst("^.*src=['\"](.*?)['\"].*$", "$1");
			if (src.equals(html)) {
				log.error("document.write only support <script src='...' ");
			} else {
				this.loadScript(src);
			}
		}

	}

	public Object createElement(String tag) {
		return new Script();
	}

	public List<Script> getScripts() {
		return Arrays.asList(new Script());
	}

	private void loadScript(String path) {
		String path2 = path.replaceAll("^/scripts/|__define__(.js)$", "");
		if (!path.equals(path2)) {
			JSIRoot root = ((RuntimeSupport) runtime).getRoot();
			String source = root.loadText(path2 + ".js");
			CompilerEnvironsImpl env = new CompilerEnvironsImpl();
			Parser parser = env.createParser();
			AstRoot node = parser.parse(source, path, 1);
			final StringBuilder buf = new StringBuilder();
			node.visit(new NodeVisitor() {
				public boolean visit(AstNode node) {
					if (node instanceof FunctionCall) {
						FunctionCall c = (FunctionCall) node;
						AstNode target = c.getTarget();
						if (target instanceof Name
								&& "require".equals(((Name) target)
										.getIdentifier())) {
							List<AstNode> args = c.getArguments();

							if (args.size() == 1) {
								AstNode arg1 = args.get(0);
								if (arg1 instanceof StringLiteral) {
									//System.out.println(arg1.toSource());
									String path =arg1.toSource();
									if (buf.length() > 0) {
										buf.append(",");
									}
									buf.append(path);
								}
							}
						}
					}
					return true;
				}

			});
			source = "$JSI.define('" + path2 + "',[" + buf
					+ "],function(require,exports){" + source + "\n})";
			System.out.println(path2);
			try {
				runtime.eval(source, path);
			} catch (RuntimeException e) {
				//System.out.println(source);
				e.printStackTrace();
				throw e;
			}

		}
	}

	public class Script {
		private String value;

		public Object getParentNode() {
			return this;// hack
		}

		public void setAttribute(String key, String value) {
			if ("src".equalsIgnoreCase(key)) {
				this.value = value;
			}
		}

		public void appendElement(Script tag) {
			if (tag.value.endsWith(".js")) {
				loadScript(tag.value);
			}
		}
	}

}
