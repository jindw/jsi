package org.xidea.template;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ParseContext extends HashMap<Object, Object>{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private URL currentURL;
	private ArrayList<Object> result = new ArrayList<Object>();

	public void append(String object) {
		result.add(object);
	}
	public void append(Object[] object) {
		result.add(object);
	}

	public void append(List<Object> items) {
		for (Object text : items) {
			if(text instanceof String){
				this.append((String)text);
			}else{

				this.append((Object[])text);
			}
			
		}
	}

	public URL getCurrentURL() {
		return currentURL;
	}

	public void setCurrentURL(URL currentURL) {
		this.currentURL = currentURL;
	}

	public List<Object> getResult() {
		ArrayList<Object> result2 = new ArrayList<Object>(result.size());
		StringBuilder buf = new StringBuilder();
		for (Object item : result) {
			if(item instanceof String){
				buf.append(item);
			}else{
				if(buf.length()>0){
					result2.add(buf.toString());
					buf.setLength(0);
				}
				result2.add((Object[])item);
			}
		}
		if(buf.length()>0){
			result2.add(buf.toString());
		}
		return result2;
	}

	public void removeLastEnd() {
		int i = result.size() ;
		while(i-- > 0){
			Object item = result.get(i);
			result.remove(i);
			if(item instanceof Object[]){
				if(((Object[])item).length == 0){
					break;
				}
			}
		}
	}
}