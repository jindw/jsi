package org.xidea.jsel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.xidea.template.Expression;

/**
 * 基于逆波兰式的表达式解析
 * @author jindw
 */
public class SimpleExpression implements Expression{

	private List<String> expression;// 结果

	// 依据输入信息创建对象，将数值与操作符放入ArrayList中
	public SimpleExpression(String input) {
		ArrayList<String> tokens = new ArrayList<String>();// 存储中序表达式
		StringTokenizer st = new StringTokenizer(input, "+-*/()", true);
		while (st.hasMoreElements()) {
			tokens.add(st.nextToken());
		}
		expression = toRight(tokens.iterator()) ;
	}

	// 将中序表达式转换为右序表达式
	private List<String> toRight(Iterator<String> tokens) {
		ArrayList<String> right = new ArrayList<String>();// 存储右序表达式
		LinkedList<String> buffer = new LinkedList<String>();
		String operator;
		while (tokens.hasNext()) {
			String item = tokens.next();
			if (Calculate.isOperator(item)) {
				if (buffer.isEmpty()
						|| item.equals("(")) {
					buffer.push(item);
				} else {
					if (item.equals(")")) {
						if (!buffer.getFirst().equals("(")) {
							operator = (String) buffer.pop();
							right.add(operator);
						}
					} else {
						if (Calculate.priority(item) <= Calculate
								.priority((String) buffer.getFirst())
								&& !buffer.isEmpty()) {
							operator = (String) buffer.pop();
							if (!operator.equals("(")){
								right.add(operator);
							}
						}
						buffer.push(item);
					}
				}
			} else{
				right.add(item);
			}
		}
		while (!buffer.isEmpty()) {
			operator = (String) buffer.pop();
			right.add(operator);
		}
		return right;
	}

	// 对右序表达式进行求值
	public Object evaluate(Map<Object,Object> context) {
		LinkedList<Object> aStack = new LinkedList<Object>();
		String is = null;
		Iterator<String> it = expression.iterator();

		while (it.hasNext()) {
			is = (String) it.next();
			if (Calculate.isOperator(is)) {
				aStack.push(Calculate.compute(is, aStack));
			} else{
				aStack.push(is);
			}
		}
		return aStack.pop();

	}

}

class Calculate {
	// 判断是否为操作符号
	public static boolean isOperator(String operator) {
		if (operator.equals("+") || operator.equals("-")
				|| operator.equals("*") || operator.equals("/")
				|| operator.equals("(") || operator.equals(")"))
			return true;
		else
			return false;
	}

	// 设置操作符号的优先级别
	public static int priority(String operator) {
		if (operator.equals("+") || operator.equals("-")
				|| operator.equals("("))
			return 1;
		else if (operator.equals("*") || operator.equals("/"))
			return 2;
		else
			return 0;
	}

	// 做2值之间的计算
	public static Object compute(String operator,LinkedList<Object> aStack) {
		try {
			String op = operator;

			Object op1 = aStack.pop();
			Object op2 = aStack.pop();
			
			double x = Double.parseDouble(String.valueOf(op1));
			double y = Double.parseDouble(String.valueOf(op2));
			double z = 0;
			if (op.equals("+"))
				z = x + y;
			else if (op.equals("-"))
				z = x - y;
			else if (op.equals("*"))
				z = x * y;
			else if (op.equals("/"))
				z = x / y;
			else
				z = 0;
			return  z;
		} catch (NumberFormatException e) {
			System.out.println("input has something wrong!");
			return "Error";
		}
	}
}
