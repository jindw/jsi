package org.xidea.el;

import static org.junit.Assert.*;

import org.junit.Test;

public class ExpressionFactoryImplTest {
	ExpressionFactoryImpl expressionFactory = new ExpressionFactoryImpl();
	@Test
	public void testOptimizeELString() {
		expressionFactory.optimizeEL("''");
		expressionFactory.optimizeEL("'['");
		try{
			expressionFactory.optimizeEL("'''");
			fail("无效字符串状态");
		}catch (Exception e) {
		}

		try{
			expressionFactory.optimizeEL("[([)]]");
			fail("无效括弧状态");
		}catch (Exception e) {
		}
	}

}
