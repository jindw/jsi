package org.xidea.template.test;

import static org.junit.Assert.*;

import java.util.HashMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xidea.jsel.Java6JSExpressionFactory;
import org.xidea.template.Expression;

public class Java6JSExpressionFactoryTest {

	private Java6JSExpressionFactory factory;
	HashMap<Object, Object> model = new HashMap<Object, Object>();
	@Before
	public void setUp() throws Exception {
		factory = new Java6JSExpressionFactory();
	}
	@After
	public void tearDown() throws Exception {
		model.clear();
	}

	@Test
	public void testExpression1() {
		Expression expression = factory.createExpression("1+1.1");
		assertEquals(expression.evaluate(model),2.1);
	}

	@Test
	public void testValueExpression() {
		model.put("v1",123);
		model.put("v2",123);
		Expression expression = factory.createExpression("v1+v2");
		assertEquals(expression.evaluate(model),246);
	}

}
