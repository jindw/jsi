package org.xidea.el.operation;

import java.util.Map;

import org.xidea.el.parser.OperatorToken;



public interface Calculater {
	// 做2值之间的计算
	/**
	 * @param it 
	 * @return skip next value
	 * @see CalculaterImpl
	 */
	@SuppressWarnings("unchecked")
	public Object compute(Map context,OperatorToken op,Object arg1,Object arg2) ;
}
