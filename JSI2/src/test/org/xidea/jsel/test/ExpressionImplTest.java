package org.xidea.jsel.test;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.xidea.jsel.ExpressionImpl;

public class ExpressionImplTest {

	@Before
	public void setUp() throws Exception {
	}
	private void test(String el,Object value,Map<String, Object> context){
		ExpressionImpl exp = new ExpressionImpl(el);
		assertEquals(value, exp.evaluate(context));
	}

	@Test
	public void testIntMath() {
		test("1+2",3,null);
		test("1+2*2",5,null);
		test("(1+2)*2",6,null);
		test("(1-2)*2",-2,null);
	}
	@Test
	public void test3op() {
		test("0?1:2",2,null);
		test("0?1+4:2*2",4,null);
		test("0?1+4:+2",2,null);
		test("(0?1+4:+2)+1",3,null);
	}
	@Test
	public void testProp() {
		Map<String, Object> context = new HashMap<String, Object>();
		context.put("var1", Arrays.asList(1,2));
		test("var1[0]+1+var1[1]",4,context);
		test("[1,2,3][1]",2,context);
		test("{aaa:123}['aaa']",123,context);
	}
}
