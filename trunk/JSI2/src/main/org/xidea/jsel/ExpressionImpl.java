package org.xidea.jsel;

import java.util.Iterator;
import java.util.Map;

import org.xidea.template.Expression;

public class ExpressionImpl implements Expression {
	private static final Calculater DEFAULT_CALCULATER = new CalculaterImpl();

	private static boolean containsDobule(Object arg1, Object arg2) {
		return false;
	}
	ExpressionTokenizer expressionTokens;
	private Calculater calculater = DEFAULT_CALCULATER;
	public ExpressionImpl(String el){
		expressionTokens = new ExpressionTokenizer(el);
	}
	public ExpressionImpl(String el,Calculater calculater){
		expressionTokens = new ExpressionTokenizer(el);
		this.calculater = calculater;
	}

	public Object evaluate(Map<Object, Object> context) {
		ValueStack stack = new ValueStack();
		ExpressionToken item = null;
		Iterator<ExpressionToken> it = expressionTokens.iterator();
		while (it.hasNext()) {
			item = (ExpressionToken) it.next();
			if (item instanceof OperatorToken) {
				Object arg1 = stack.pop();
				Object arg2 = null;
				if(((OperatorToken)item).getLength()>1){
					arg2 = stack.pop();
				}
				Object result = calculater.compute((OperatorToken) item,arg1,arg2);
				if(result == Calculater.EVAL){
					
				}else{
					stack.push(result);
				}
			} else{
				if(item instanceof VarToken){
					stack.push(context.get(((VarToken)item).getValue()));
				}else{
					stack.push(((ConstantsToken)item).getValue());
				}
			}
		}
		return stack.pop();
	}
	
}