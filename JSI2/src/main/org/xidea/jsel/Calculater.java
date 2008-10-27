package org.xidea.jsel;


public interface Calculater {
	// 做2值之间的计算
	/**
	 * @param it 
	 * @return skip next value
	 */
	public Object compute(OperatorToken op,Object arg1,Object arg2) ;
}
