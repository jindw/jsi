package org.xidea.el.operation.test;

import static org.junit.Assert.*;
import org.junit.Test;
import org.xidea.el.Expression;
import org.xidea.el.ExpressionFactory;
import org.xidea.el.ExpressionFactoryImpl;


public class ECMA262GlobalTest {
	ExpressionFactory factory = new ExpressionFactoryImpl();

	@Test
	public void testEncode() throws Exception{
		//Expression el = factory.createEL("'金大为'.substring(1,2)");
		Expression el = factory.createEL("encodeURIComponent('金大为')");
		String encoded = (String)el.evaluate(null);
		assertEquals("%E9%87%91%E5%A4%A7%E4%B8%BA", encoded);
		el = factory.createEL("decodeURIComponent('a%E9%87%91%E5%A4%A7%E4%B8%BA')");
		assertEquals("a金大为", el.evaluate(null));
	}
	@Test
	public void testIsNaN() throws Exception{
		//Expression el = factory.createEL("'金大为'.substring(1,2)");
		Expression el = factory.createEL("isNaN(0/0)");
		assertEquals(true, el.evaluate(null));
	}
	
}
