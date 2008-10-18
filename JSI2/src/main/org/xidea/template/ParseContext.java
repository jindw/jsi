package org.xidea.template;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ParseContext extends HashMap<Object, Object>{
	private URL currentURL;
	private ArrayList<Object> result = new ArrayList<Object>();

	public void append(char text) {
		this.append(String.valueOf(text));
	}

	public void append(Object object) {
		result.add(object);
	}

	public void append(List<Object> items) {
		for (Object text : items) {
			this.append(text);
		}
	}

	public URL getCurrentURL() {
		return currentURL;
	}

	public void setCurrentURL(URL currentURL) {
		this.currentURL = currentURL;
	}

	public List<Object> getResult() {
		return result;
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