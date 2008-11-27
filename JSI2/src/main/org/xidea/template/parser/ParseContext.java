package org.xidea.template.parser;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.xidea.template.Template;

public class ParseContext extends HashMap<Object, Object> {
	private static final long serialVersionUID = 1L;
	private URL currentURL;
	private ArrayList<Object> result = new ArrayList<Object>();
	private int depth = -1;
	private boolean reserveSpace;
	private boolean format = true;

	/**
	 * 在XMLParser中判断平设置，Core标签将缩进做了回退处理
	 * 
	 * @return
	 */
	public int getDepth() {
		return depth;
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}

	public boolean isFormat() {
		return format;
	}

	public void setFormat(boolean format) {
		this.format = format;
	}

	public boolean isReserveSpace() {
		return reserveSpace;
	}

	public void setReserveSpace(boolean keepSpace) {
		this.reserveSpace = keepSpace;
	}

	public URL getCurrentURL() {
		return currentURL;
	}

	public void setCurrentURL(URL currentURL) {
		this.currentURL = currentURL;
	}

	public void append(String text) {
		if (text != null && text.length() > 0) {
			result.add(text);
		}
	}

	public void append(Object[] object) {
		result.add(object);
	}

	public void appendIndent() {
		if (this.format && !this.reserveSpace) {
			int pos = result.size() - 1;
			int depth = this.depth;
			if (depth > 0 && pos > 0) {
				char[] data = new char[depth];
				while (depth-- > 0) {
					data[depth] = '\t';
				}
				result.add("\r\n" + new String(data));
			}

		}
	}

	public void appendList(List<Object> items) {
		for (Object text : items) {
			if (text instanceof String) {
				this.append((String) text);
			} else {

				this.append((Object[]) text);
			}

		}
	}

	public void removeLastEnd() {
		int i = result.size();
		while (i-- > 0) {
			Object item = result.get(i);
			result.remove(i);
			if (item instanceof Object[]) {
				if (((Object[]) item).length == 0) {
					break;
				}
			}
		}
	}


	@SuppressWarnings("unchecked")
	public List<Object> getResultTree() {
		ArrayList<Object> result2 = getResult();
		ArrayList<ArrayList<Object>> stack = new ArrayList<ArrayList<Object>>();
		ArrayList<Object> current = new ArrayList<Object>();
		stack.add(current);
		int stackTop = 0;
		for (Object item : result2) {
			if (item instanceof String) {
				//System.out.println(item);
				current.add(item);
			} else {
				Object[] cmd = (Object[]) item;

				//System.out.println(Arrays.asList(cmd));
				if (cmd.length == 0) {
					ArrayList<Object> children = stack.remove(stackTop--);
					current = stack.get(stackTop);

					((ArrayList) current.get(current.size() - 1)).set(1,
							children);
				} else {
					int type = (Integer) cmd[0];
					if(type == Template.ELSE_TYPE){
						ArrayList<Object> children = stack.remove(stackTop--);
						current = stack.get(stackTop);
						((ArrayList) current.get(current.size() - 1)).set(1,
								children);
					}
					
					ArrayList<Object> cmd2 = new ArrayList<Object>(
							cmd.length + 1);
					cmd2.add(cmd[0]);
					current.add(cmd2);
					switch (type) {
					case Template.VAR_TYPE:
						if (cmd[3] != null) {
							break;
						}
					case Template.IF_TYPE:
					case Template.ELSE_TYPE:
					case Template.FOR_TYPE:
//					case Template.IF_STRING_IN_TYPE:
						cmd2.add(null);
						stackTop++;
						stack.add(current = new ArrayList<Object>());
					}
					for (int i = 1; i < cmd.length; i++) {
						cmd2.add(cmd[i]);
					}

				}
			}
		}
		return stack.get(0);
	}

	public ArrayList<Object> getResult() {
		ArrayList<Object> result2 = new ArrayList<Object>(result.size());
		StringBuilder buf = new StringBuilder();
		for (Object item : result) {
			if (item instanceof String) {
				buf.append(item);
			} else {
				if (buf.length() > 0) {
					result2.add(buf.toString());
					buf.setLength(0);
				}
				result2.add((Object[]) item);
			}
		}
		if (buf.length() > 0) {
			result2.add(buf.toString());
		}
		return result2;
	}

}