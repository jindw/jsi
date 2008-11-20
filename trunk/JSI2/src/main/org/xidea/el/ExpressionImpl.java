package org.xidea.el;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xidea.el.operation.Calculater;
import org.xidea.el.operation.CalculaterImpl;
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

	public ExpressionImpl(String el) {
		this(el, DEFAULT_CALCULATER);
	}

	public ExpressionImpl(String el, Calculater calculater) {
		this.source = el;
		ExpressionTokenizer expressionTokens = new ExpressionTokenizer(el);
		this.calculater = calculater;
		this.expression = expressionTokens.toReversedArray();
	}

	@SuppressWarnings("unchecked")
	public Object evaluate(Map<? extends Object, ? extends Object> context) {
		if (context == null) {
			context = Collections.EMPTY_MAP;
		}
		ValueStack stack = new ValueStack();
		evaluate(stack, expression, context);
		return stack.pop();
	}

	@SuppressWarnings("unchecked")
	private void evaluate(ValueStack stack, ExpressionToken[] tokens,
			Map context) {
		ExpressionToken item = null;
		int i = tokens.length;
		while (i-- > 0) {
			item = (ExpressionToken) tokens[i];
			if (item instanceof OperatorToken) {
				Object arg2 = null;
				Object arg1 = null;
				int length = ((OperatorToken) item).getLength();
				if (length > 1) {
					arg2 = stack.pop();
					arg1 = stack.pop();
				} else if (length == 1) {
					arg1 = stack.pop();
				}
				switch (item.getType()) {
				case ExpressionToken.OP_PARAM_JOIN:
					((List) arg1).add(arg2);
					stack.push(arg1);
					break;
				case ExpressionToken.OP_MAP_PUSH:
					((Map) arg1).put(((OperatorToken) item).getParam(), arg2);
					stack.push(arg1);
					break;
				default:
					Object result = calculater.compute((OperatorToken) item,
							arg1, arg2);
					if (result instanceof LazyToken) {
						evaluate(stack, ((LazyToken) result).getChildren(),
								context);
					} else {
						stack.push(result);
					}
				}
			} else {
				stack.push(getTokenValue(context, item));
			}
		}
	}

	protected Object getTokenValue(Map<Object, Object> context,
			ExpressionToken item) {
		switch (item.getType()) {
		case ExpressionToken.VALUE_NEW_LIST:
			return new ArrayList<Object>();
		case ExpressionToken.VALUE_NEW_MAP:
			return new HashMap<Object, Object>();
		case ExpressionToken.VALUE_VAR:
			String value = ((VarToken) item).getValue();
			if ("this".equals(value)) {
				return context;
			} else {
				Object result = context.get(value);
				if (result == null && !context.containsKey(value)) {
					result = calculater.getGlobalInvocable(value);
				}
				return result;
			}
		case ExpressionToken.VALUE_LAZY:
			return (item);
		default:
			return (((ValueToken) item).getValue());
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

	public Object top() {
		return data[pos];
	}

	public Object pop() {
		return data[pos--];
	}

	public Object push(Object value) {
		pos++;
		if (pos >= data.length) {
			Object[] data2 = new Object[data.length * 2];
			System.arraycopy(data, 0, data2, 0, data.length);
			data = data2;
		}
		return data[pos] = value;
	}

	public boolean isEmpty() {
		return pos < 0;
	}

}
