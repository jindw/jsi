package org.xidea.jsi.impl.v3.test;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;

import org.junit.Test;
import org.xidea.jsi.impl.v3.ClasspathRoot;
import org.xidea.jsi.impl.v3.RuntimeSupport;

public class DebugTest {
	ClassLoader loader ;
	File file = new File("D:\\workspace\\Lite2/build/dest/require/");
	final RuntimeSupport rs = (RuntimeSupport) RuntimeSupport.create();
	{
			try {
				loader = new URLClassLoader(new URL[]{file.toURI().toURL()});
				rs.setRoot(new ClasspathRoot(loader,"utf-8"){
					public String loadText(String absPath) {
						File f = new File(file,absPath);
						if(f.exists()){
							try {
								return loadTextAndClose(new FileInputStream(f), encoding);
							} catch (FileNotFoundException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						return loadText(absPath, loader, encoding);
					}
				});
			} catch (MalformedURLException e) {
				e.printStackTrace();
			};
	}
	@Test
	public void test() throws InterruptedException{
		//Main.mainEmbedded( ContextFactory.getGlobal(),(Scriptable) rs.getGlobals(),"JSI Debug");
		rs.eval("$export('org/xidea/el/expression')");
	}
	@Test
	public void testAll(){
		final URI base = file.toURI();
		file.listFiles(new FileFilter() {
			int index = 0;
			public boolean accept(File f) {
				if(f.isDirectory()){
					f.listFiles(this);
				}else{
					String path = base.relativize(f.toURI()).getPath();
					System.out.println(index++ +":"+path);
					
					rs.eval("$export('"+path.replaceFirst("\\.js$", "")+"')");
					return false;
				}
				return false;
			}
		});
	}

}
