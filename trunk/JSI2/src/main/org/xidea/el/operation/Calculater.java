package org.xidea.el.operation;

import java.lang.reflect.Method;
import java.util.Map;

import org.xidea.el.parser.OperatorToken;



/**
 * 做2值之间的计算。
 * 三元运算符，需要转化为二元表示
 * 一元何零元运算符，null自动补全
 * @author jindw
 */
public interface Calculater {

	/**
	 * @param context 运算变量表
	 * @param op 操作符对象
	 * @param arg1 参数1
	 * @param arg2 参数2
	 * @return 运算结果
	 */
	@SuppressWarnings("unchecked")
	public Object compute(Map context,OperatorToken op,Object arg1,Object arg2) ;

	public void addInvocable(String name,Method method);
	public void addInvocable(Class<?> clazz,String name,Method method);
}
