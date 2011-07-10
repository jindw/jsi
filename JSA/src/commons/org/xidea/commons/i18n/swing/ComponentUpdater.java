/*
 * Created on 2004-10-23
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.xidea.commons.i18n.swing;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

/**
 * @project fix
 * @author 金大为
 */
@SuppressWarnings("unchecked")
public interface ComponentUpdater {

	public Component[] update(Map replace, Component component);
    
    public static class java_awt_Frame implements ComponentUpdater {
        public Component[] update(Map replace, Component component) {
            Frame frame = (Frame)component;
            String ot = frame.getTitle();
            Object nt = replace.get(ot);
            if(nt instanceof String){
                frame.setTitle((String) nt);
            }
            return frame.getComponents();
        }

    }
    public static class java_awt_Dialog implements ComponentUpdater {

		public Component[] update(Map replace, Component component) {
			Dialog dialog = (Dialog)component;
            String ot = dialog.getTitle();
            Object nt = replace.get(ot);
            if(nt instanceof String){
                dialog.setTitle((String) nt);
            }
            return dialog.getComponents();
		}

	}
	public static class javax_swing_AbstractButton implements ComponentUpdater{
        public Component[] update(Map replace, Component component){
            AbstractButton button = (AbstractButton)component;
            String ot = button.getText();
            Object nt = replace.get(ot);
            if(nt instanceof String){
                button.setText((String) nt);
            }
            return button.getComponents();
        }
    }
    public static class javax_swing_JLabel implements ComponentUpdater{
        public Component[] update(Map replace, Component component){
        	JLabel label = (JLabel)component;
            String ot = label.getText();
            Object nt = replace.get(ot);
            if(nt instanceof String){
                label.setText((String) nt);
            }
            return label.getComponents();
        }
    }
    public static class javax_swing_JTable implements ComponentUpdater{
        public Component[] update(Map replace, Component component){
        	JTable table = (JTable)component;
        	TableColumnModel clumnModel = table.getTableHeader().getColumnModel();
        	int count  = clumnModel.getColumnCount();
        	for (int i = 0; i < count; i++) {
        		TableColumn column = clumnModel.getColumn(i);
                Object nt = replace.get(column.getHeaderValue());
                if(nt instanceof String){
                	column.setHeaderValue(nt);
                }
			}
            return table.getComponents();
        }
    }
    public static class javax_swing_JTabbedPane implements ComponentUpdater{
        public Component[] update(Map replace, Component component){
        	JTabbedPane tabbedPane = (JTabbedPane)component;
        	int titleCount = tabbedPane.getTabCount();
        	for (int i = 0; i < titleCount; i++) {
        		String ot = tabbedPane.getTitleAt(i);
                Object nt = replace.get(ot);
                if(nt instanceof String){
                    tabbedPane.setTitleAt(i,(String) nt);
                }
			}
            
            return tabbedPane.getComponents();
        }
    }
    public static class javax_swing_JMenu extends javax_swing_AbstractButton{
        public Component[] update(Map replace, Component component){
            Component[] cs1 = super.update(replace, component);
            Component[] cs2 =  ((JMenu)component).getMenuComponents() ;
            //System.out.println(cs1.length+"/"+cs2.length);
            if(cs1==null||cs1.length ==0){
                return cs2;
            }
            if(cs2==null||cs2.length ==0){
                return cs1;
            }
            Component[] cs = new Component[cs1.length+cs2.length];
            System.arraycopy(cs1,0,cs,0,cs1.length);
            System.arraycopy(cs2,0,cs,cs1.length,cs2.length);
            //System.out.println(cs.length);
            return cs;
        }
    }
}
