package org.xidea.jsel.test;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.xidea.jsel.ExpressionImpl;

public class ExpressionImplTest {

	@Before
	public void setUp() throws Exception {
	}
	private void test(String el,Object value){
		ExpressionImpl exp = new ExpressionImpl(el);
		assertEquals(value, exp.evaluate(null));
	}

	@Test
	public void testIntMath() {
		test("1+2",3);
		test("1+2*2",5);
		test("(1+2)*2",6);
		test("(1-2)*2",-2);
	}
	@Test
	public void test3op() {
		test("0?1:2",2);
		test("0?1+4:2*2",4);
		test("0?1+4:+2",2);
		test("(0?1+4:+2)+1",3);
	}
}
