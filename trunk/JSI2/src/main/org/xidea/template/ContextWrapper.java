package org.xidea.template;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class ContextWrapper implements Map<Object, Object>{
	private Map<Object, Object> context;
	private Map<Object, Object> stack ;

	public ContextWrapper(Map<Object, Object> context){
		this.context = context;
	}
	
	public Object get(Object key) {
		if(stack!=null && stack.containsKey(key)){
			return stack.get(key);
		}
		return PropertyExpression.getValue(context, key);
	}

	public Object put(Object key, Object value) {
		if(stack==null ){
			stack = new HashMap<Object, Object>();
		}
		return stack.put(key, value);
	}


	public void clear() {
		if(stack!=null ){
			stack.clear();
		}
	}

	public boolean containsKey(Object key) {
		return context.containsKey(key) || stack!=null && stack.containsKey(key) ;
	}


	public boolean containsValue(Object value) {
		return context.containsValue(value) || stack!=null && stack.containsValue(value) ;
	}


	public Set<java.util.Map.Entry<Object, Object>> entrySet() {
		Set<java.util.Map.Entry<Object, Object>> context = this.context.entrySet();
		if(stack != null){
			context = new HashSet<Entry<Object,Object>>(context);
			context.addAll(stack.entrySet());
		}
		return (Set<Entry<Object, Object>>) context;
	}


	public boolean isEmpty() {
		return context.isEmpty()&& stack!=null && stack.isEmpty() ;
	}


	public Set<Object> keySet() {
		Set<Object> context = this.context.keySet();
		if(stack != null){
			context = new HashSet<Object>(context);
			context.addAll(stack.keySet());
		}
		return (Set<Object>) context;
	}


	public void putAll(Map<? extends Object, ? extends Object> m) {
		if(stack == null){
			stack = new HashMap<Object, Object>();
		}
		stack.putAll(m);
	}


	public Object remove(Object key) {
		if(stack != null){
		    return stack.remove(key);
		}
		return null;
	}


	public int size() {
		int size = this.context.size();
		if(stack != null){
			size += stack.size();
		}
		return size;
	}


	public Collection<Object> values() {
		Collection<Object> context = this.context.values();
		if(stack != null){
			context = new HashSet<Object>(context);
			context.addAll(stack.values());
		}
		return context;
	}

	
}
