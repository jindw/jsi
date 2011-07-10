package org.jside;

import java.awt.event.MouseEvent;
import java.io.File;

public interface JSideListener<T>{
	public interface Exit  extends JSideListener<Object>{}
	public interface TrayClick  extends JSideListener<MouseEvent>{}
	public interface DockClick  extends JSideListener<MouseEvent>{}
	/**
	 * 发出方：DockUI
	 */
	public interface BeforeFileOpen extends JSideListener<File>{}
	
	public interface FileOpen extends JSideListener<File>{}
	
	/**
	 * 发出方：JSideWebServer#reset
	 * 接受方：JSA...
	 */
	public interface WebServerReset extends JSideListener<JSideWebServer>{}
	
	public interface WebRootChange extends JSideListener<File>{}

	
	public boolean execute(T source);
}
