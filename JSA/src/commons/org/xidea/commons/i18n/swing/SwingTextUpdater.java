/*
 * Created on 2004-10-23
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.xidea.commons.i18n.swing;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Frame;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JTabbedPane;
import javax.swing.JTable;

/**
 * @project fix
 * @author 金大为
 */
public class SwingTextUpdater {
    private Map updaters =new HashMap();
    public SwingTextUpdater(){
        this.setUpdater(AbstractButton.class,new ComponentUpdater.javax_swing_AbstractButton());
        this.setUpdater(JMenu.class,new ComponentUpdater.javax_swing_JMenu());
        this.setUpdater(Frame.class,new ComponentUpdater.java_awt_Frame());
        this.setUpdater(Dialog.class,new ComponentUpdater.java_awt_Dialog());
        this.setUpdater(JTabbedPane.class, new ComponentUpdater.javax_swing_JTabbedPane());
        this.setUpdater(JLabel.class, new ComponentUpdater.javax_swing_JLabel());
        this.setUpdater(JTable.class, new ComponentUpdater.javax_swing_JTable());
    }
    public void update(Map replace,Component component){
        if(component instanceof Container){
            updateContainer(replace,(Container)component);
        }else{
            updateComponent(replace,component);
        }
    }
    public void updateContainer(Map replace,Container container){
        //System.out.println(container);
        Component[] subs = updateComponent(replace,container);
        if(subs!=null){
            for(int j=0;j<subs.length;j++){
               update(replace,subs[j]);
            }

        }
    }
    public void setUpdater(Class cls,ComponentUpdater updater){
        updaters.put(cls,updater);
    }
    
    /**
     * @param replace
     * @param component
     */
    protected Component[] updateComponent(Map replace, Component component) {
        // TODO Auto-generated method stub
        Class cls = component.getClass();
        ComponentUpdater updater = (ComponentUpdater) updaters.get(cls);
        while(updater==null&&cls!=Component.class){
            cls =cls.getSuperclass();
            updater = (ComponentUpdater) updaters.get(cls);
        }
        if(updater!=null){
            return updater.update(replace, component);
        }
        else if(component instanceof Container){
            return ((Container)component).getComponents();
        }
        return null;
    }
}
