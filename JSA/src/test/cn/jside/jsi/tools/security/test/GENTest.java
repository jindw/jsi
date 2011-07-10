package cn.jside.jsi.tools.security.test;
//package cn.jside.jsi.tools.test.security;
//
//import org.junit.Test;
//
//import cn.jside.jsi.tools.rhino.DebugTool;
//import cn.jside.jsi.tools.security.GEN;
//
//
//public class GENTest {
//
//	@Test
//	public void testGEN() {
//		String data = GEN.DATA.replaceAll("[\\s/\\*]", "").toUpperCase();
//		for (int i = 1; i < 16; i++) {
//			char alpha = Integer.toHexString(i).charAt(0);
//			String value = data + alpha;
//			String back  = value = GEN.getHashString(value);
//			for (int j = 1; j < 4; j++) {
//				value = GEN.getHashString(value);
//				if(value.charAt(0)<alpha){
//					String key = back+alpha;
//					DebugTool.info("key:");
//					DebugTool.info(GEN.checkSign(GEN.DATA,key));
//					DebugTool.info(key);
//					return ;
//				}
//				back = value;
//			}
//		}
//	}
//
//}
