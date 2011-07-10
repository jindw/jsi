package org.jside.jsi.tools.util;


import java.util.List;

public interface ScriptNode{
	public static final int TYPE_SCRIPT = 0;
	public static final int TYPE_FUNCTION = 1;
	public static final int TYPE_VARIABLE = 2;
	public static final int TYPE_CATCH = 3;
	public String getName();
	public int getType();
	public ScriptNode getParent();
    public int getStartLine();
    public int getEndLine();
    public List<ScriptNode> getChildren();
}
