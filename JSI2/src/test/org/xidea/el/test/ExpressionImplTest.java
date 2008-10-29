package org.xidea.el.test;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.xidea.el.ExpressionImpl;

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
		test("1/2",0.5,null);
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
	}
	@Test
	public void testListConstructor() {
		Map<String, Object> context = new HashMap<String, Object>();
		test("[1,2,3][1]",2,context);
	}
	@Test
	public void testMapConstructor() {
		Map<String, Object> context = new HashMap<String, Object>();
		test("{aaa:1,'bb':2}['aaa']",1,context);
		test("{aaa:1,bb:2}['bb']",2,context);
	}
	@Test
	public void testListMap() {
		Map<String, Object> context = new HashMap<String, Object>();
		test("{aaa:1,'bb':[1,3,2]}['bb'][0]",1,context);
		test("{aaa:1,'bb':[1,3,2]}['bb']['1']",3,context);
		test("[1,{aa:2}][1]['aa']",2,context);
	}
	@Test
	public void testMethod() {
		Map<String, Object> context = new HashMap<String, Object>();
		test("'123'.startsWith('12')",true,context);
		test("'123'.endsWith('12')",false,context);
	}
}
