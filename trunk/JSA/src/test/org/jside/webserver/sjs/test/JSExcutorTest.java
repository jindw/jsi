package org.jside.webserver.sjs.test;


import org.jside.webserver.sjs.JSExcutor;
import org.junit.Before;
import org.junit.Test;

public class JSExcutorTest {

	@Before
	public void setUp() throws Exception {
	}
	@Test
	public void init() throws Exception {
		JSExcutor.getCurrentInstance().eval("print(template)", null);
	}

}
