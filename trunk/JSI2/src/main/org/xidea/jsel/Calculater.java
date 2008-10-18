package org.xidea.jsel;

import java.util.Iterator;


public interface Calculater {
	// 做2值之间的计算
	/**
	 * @param it 
	 * @return skip next value
	 */
	public boolean compute(OperatorToken op,ValueStack stack, Iterator<ExpressionToken> it) ;
}
