package org.jside.jsi.tools.util.test;

import java.util.HashSet;

import org.jside.jsi.tools.util.IDGenerator;
import org.junit.Test;

import cn.jside.jsi.tools.rhino.DebugTool;


public class IDGeneratorTest {
	
	public void test2(){
		//for test
		IDGenerator gen  = new IDGenerator(null,10,"_");
		HashSet<String> set = new HashSet<String>();
		HashSet<String> set2 = new HashSet<String>();
		for (int i = 0; i < /*1728*12+*/1728+144+12; i++) {
			String id = gen.newId();
			if(set.contains(id)){
				set2.add(id);
			}
			set.add(id);
			DebugTool.info(id);
		}
		DebugTool.info("");
		DebugTool.info(set2);
		DebugTool.info(gen.BASE);
	}

	@Test
	public void testIDGenerator() {
		int k = 0;
		IDGenerator gen = new IDGenerator();
		for (int i = 0; i < 100; i++) {
			for (int j = 0; j < 100; j++, k++) {
				System.out.println(gen.newId());
				System.out.println(',');
			}
		}
	}

}
