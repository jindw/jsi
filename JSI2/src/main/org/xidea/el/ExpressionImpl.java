package org.xidea.el;

import java.util.Map;

import org.xidea.el.parser.ConstantsToken;
import org.xidea.el.parser.ExpressionToken;
import org.xidea.el.parser.ExpressionTokenizer;
import org.xidea.el.parser.LazyToken;
import org.xidea.el.parser.OperatorToken;
import org.xidea.el.parser.VarToken;
import org.xidea.template.Expression;

public class ExpressionImpl implements Expression {
	private static final Calculater DEFAULT_CALCULATER = new CalculaterImpl();
	private final Calculater calculater;
	private ExpressionToken[] expression;
	public ExpressionImpl(String el){
		this(el,DEFAULT_CALCULATER);
	}
	public ExpressionImpl(String el,Calculater calculater){
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
					stack.push(context.get(((VarToken)item).getValue()));
				}else if(item instanceof ConstantsToken){
					stack.push(((ConstantsToken)item).getValue());
				}else if(item instanceof LazyToken){
					stack.push(item);
				}else{
					throw new RuntimeException("无效Token"+item);
				}
			}
		}
	}
}