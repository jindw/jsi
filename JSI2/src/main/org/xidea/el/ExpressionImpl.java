package org.xidea.el;

import java.util.Map;

import org.xidea.el.parser.ValueToken;
import org.xidea.el.parser.ExpressionToken;
import org.xidea.el.parser.ExpressionTokenizer;
import org.xidea.el.parser.LazyToken;
import org.xidea.el.parser.OperatorToken;
import org.xidea.el.parser.VarToken;

public class ExpressionImpl implements Expression {
	private static final Calculater DEFAULT_CALCULATER = new CalculaterImpl();
	private final Calculater calculater;
	private ExpressionToken[] expression;
	private String source;
	public ExpressionImpl(String el){
		this(el,DEFAULT_CALCULATER);
	}
	public ExpressionImpl(String el,Calculater calculater){
		this.source = el;
		ExpressionTokenizer expressionTokens = new ExpressionTokenizer(el);
		this.calculater = calculater;
		this.expression = expressionTokens.toArray();
	}

	@SuppressWarnings("unchecked")
	public Object evaluate(Map context) {
		ValueStack stack = new ValueStack();
		evaluate(stack, expression, context);
		return stack.pop();
	}
	private void evaluate(ValueStack stack,ExpressionToken[] tokens,Map<Object, Object> context) {
		ExpressionToken item = null;
		int i = tokens.length;
		while (i-->0) {
			item = (ExpressionToken) tokens[i];
			if (item instanceof OperatorToken) {
				Object arg2 = null;
				Object arg1 = null;
				int length = ((OperatorToken)item).getLength();
				if(length>1){
					arg2 = stack.pop();
					arg1 = stack.pop();
				}else if(length == 1){
					arg1 = stack.pop();
				}
				Object result = calculater.compute((OperatorToken) item,arg1,arg2);
				if(result instanceof LazyToken){
					evaluate(stack, ((LazyToken)result).getChildren(), context);
				}else{
					stack.push(result);
				}
			} else{
				if(item instanceof VarToken){
					String value = ((VarToken)item).getValue();
					stack.push("this".equals(value)?context:context.get(value));
				}else if(item instanceof ValueToken){
					stack.push(((ValueToken)item).getValue());
				}else if(item instanceof LazyToken){
					stack.push(item);
				}else{
					throw new RuntimeException("无效Token"+item);
				}
			}
		}
	}
	@Override
	public String toString() {
		return source;
	}
}

class ValueStack {
	private static int pos = -1;
	private Object[] data = new Object[2];  
	public Object top(){
		return data[pos];
	}
	public Object pop(){
		return data[pos--];
	}
	public Object push(Object value){
		pos++;
		if(pos >= data.length){
			Object[] data2 = new Object[data.length *2];
			System.arraycopy(data, 0, data2, 0, data.length);
			data = data2;
		}
		return data[pos] = value;
	}
	public boolean isEmpty(){
		return pos<0;
	}

}
