package org.xidea.jsel;

import java.util.Iterator;


public interface Calculater {
	public Object EVAL = new Object();
	public Object EVAL_NEXT = new Object();
	// 做2值之间的计算
	/**
	 * @param it 
	 * @return skip next value
	 */
	public Object compute(OperatorToken op,Object arg1,Object arg2) ;
}
