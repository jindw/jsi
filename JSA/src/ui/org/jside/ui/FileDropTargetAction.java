package org.jside.ui;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.File;
import java.io.IOException;
import java.util.List;

public abstract class FileDropTargetAction implements DropTargetListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private File file;

	protected abstract boolean accept(File file);
//	{
//		//if (file.getName().toLowerCase().endsWith(".js")) { //$NON-NLS-1$
//		if(file.isDirectory()){
//			return true;
//		} else {
//			return false;
//		}
//	}
	protected abstract void openFile(File file) throws IOException ;
	public void dragEnter(DropTargetDragEvent dtde) {
		file = null;
		dtde.acceptDrag(DnDConstants.ACTION_REFERENCE);
		// for jdk1.4
		Transferable transferable = dtde.getTransferable();
		try {
			if (transferable != null && !accept(transferable)) {
				dtde.rejectDrag();
			}
		} catch (Exception e) {
		}
	}

	@SuppressWarnings("unchecked")
	protected boolean accept(// DropTargetDragEvent dtde,
			Transferable transferable) {
		if (this.file == null) {
			DataFlavor[] dataFlavors = transferable.getTransferDataFlavors();
			for (int i = 0; i < dataFlavors.length; i++) {
				try {
					DataFlavor dataFlavor = dataFlavors[i];
					
					if (dataFlavor.isFlavorJavaFileListType()) {
						List<File> list = (List<File>) transferable
								.getTransferData(dataFlavors[i]);
						File file = list.get(0);
						if( accept(file)){
							this.file = file;
							return true;
						}else{
							return false;
						}
					}
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
			return false;
		} else {
			return true;
		}
	}

	public void dragOver(DropTargetDragEvent dtde) {

	}

	public void dropActionChanged(DropTargetDragEvent dtde) {
	}

	public void dragExit(DropTargetEvent dte) {
	}

	public void drop(DropTargetDropEvent dtde) {
		dtde.acceptDrop(DnDConstants.ACTION_REFERENCE);
		if (accept(dtde.getTransferable())) {
			dtde.dropComplete(true);
			try {
				openFile(file);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			dtde.rejectDrop();
		}

	}
}