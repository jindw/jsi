package org.jside.webserver.sjs.test;


import java.util.List;

import org.jside.webserver.sjs.AdapterFactory;
import org.jside.webserver.sjs.JSExcutor;
import org.junit.Before;
import org.junit.Test;

public class AdapterFactoryTest {
	private  AdapterFactory adaptor = new AdapterFactory();

	@Before
	public void setUp() throws Exception {
	}
	@Test
	public void testNumnber() throws Exception {
		Object o = JSExcutor.getCurrentInstance().eval("[0]", null);
		System.out.println(((List)adaptor.toJava(o)).get(0).getClass());
	}

}
