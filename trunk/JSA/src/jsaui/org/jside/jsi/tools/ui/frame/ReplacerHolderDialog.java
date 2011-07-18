package org.jside.jsi.tools.ui.frame;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;

import org.jside.jsi.tools.JSA;
import org.jside.jsi.tools.JavaScriptAnalysisResult;
import org.jside.jsi.tools.JavaScriptCompressionAdvisor;
import org.jside.jsi.tools.JavaScriptCompressor;
import org.jside.jsi.tools.ui.Messages;
import org.jside.jsi.tools.util.IDGenerator;

public class ReplacerHolderDialog extends JDialog {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static ReplacerHolderDialog instance;

	public static ReplacerHolderDialog getInstance() {
		if(instance == null){
			instance = new ReplacerHolderDialog();
		}
		return instance;
	}
	private boolean isUnsafeScript;
	private Collection<String> external = Collections.emptySet();
	private Collection<String> reserveds = Collections.emptySet();
	private DefaultTableModel dataModel = new DefaultTableModel(0,2){
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public boolean isCellEditable(int row, int column){
			if(!isUnsafeScript && column ==1){
				Object o = this.getValueAt(row, 0);
				if(!external.contains(o) && !reserveds.contains(o) ){
					return true;
				}
			}
			return false;
		}
	};
	private JTable table = new JTable();
	
	private JCheckBox confuseBox = new JCheckBox();
	private JTextField perfixText = new JTextField();

	private JTextArea resultArea = new JTextArea();
	

	public JavaScriptCompressionAdvisor getGlobalReplacer(String path, String source) {
		this.reset(path,source);
		this.pack();
		//System.out.println(path);
		int rowCount = dataModel.getRowCount();
		//dataModel.fireTableDataChanged();
		final HashMap<String,String> map = new HashMap<String,String>();
		for (int i = 0; i < rowCount; i++) {
			String value = (String) dataModel.getValueAt(i,1);
			if(value != null ){
				if((value=value.trim()).length() >0){
					map.put((String)dataModel.getValueAt(i, 0),value);
				}
			}
		}
		//System.out.println(map);
		String perfix = perfixText.getText().trim();
		final boolean confuse = confuseBox.isSelected();
		final IDGenerator gen;
		if(perfix.length() > 0){
			gen= new IDGenerator(perfix,0,null);
		}else{
			gen= new IDGenerator();
		}
		return new JavaScriptCompressionAdvisor(){
			public String getReplacedName(String oldValue,boolean external) {
				String value = map.get(oldValue);
				if(value == null){
					if(confuse){
						return null;
					}else{
						return oldValue;
					}
				}
				return value;
			}
			public String newVaribaleName() {
				return gen.newId();
			}
			
		};
	}
	
	private void reset(String path,String source){
		int i = dataModel.getRowCount();
		while(i-->0){
			dataModel.removeRow(i);
		}
		JavaScriptCompressor compressor=JSA.getCompressor();
		//analyser.analyse(source, path);
		JavaScriptAnalysisResult analyser = JSA.doAnalyse(compressor, source, path,this.resultArea);
		Collection<String> locals = analyser.getLocalVars();
		external = analyser.getExternalVars();
		reserveds = analyser.getReservedVars();
		isUnsafeScript = analyser.isUnsafe();
		//System.out.println(external);
		//System.out.println(locals);
		//System.out.println(reserveds);
		for (Iterator<String> it = new TreeSet<String>(locals).iterator(); it.hasNext();) {
			String name = it.next();
			String value ;
			if(isUnsafeScript){
				value = Messages.ui.unsafeLabel;//$NON-NLS-1$
			}else if(reserveds.contains(name)){
				value = Messages.ui.protectedLabel;//$NON-NLS-1$
			}else{
				value = ""; //$NON-NLS-1$
			}
			dataModel.addRow(new Object[]{name,value});
		}
		this.setVisible(true);
		//table.setModel(dataModel);
	}
	public ReplacerHolderDialog() {
		super(JSAFrame.getInstance(), Messages.ui.globalVariableMap, true); //$NON-NLS-1$
		initialize();
		this.setSize(new Dimension(580, 460));
		// this.setPreferredSize(new Dimension(580, 460));
		this.setLocationRelativeTo(JSAFrame.getInstance());
		this.setVisible(false);
		this.setResizable(true);
	}
	private void hidden(){
		this.setVisible(false);
	}

	public void initialize() {
		JRootPane rootPane = this.getRootPane();
		rootPane.setLayout(new BorderLayout());
		rootPane.setPreferredSize(new Dimension(400,500));
		table.setModel(dataModel);
		JTableHeader header = table.getTableHeader();
		TableColumnModel columnModel = header.getColumnModel();
		columnModel.getColumn(0).setHeaderValue(Messages.ui.variableNameLabel);//$NON-NLS-1$
		columnModel.getColumn(1).setHeaderValue(Messages.ui.confusedValueLabel);//$NON-NLS-1$
		final JTabbedPane tabPane = new JTabbedPane();
		tabPane.add(new JScrollPane(table), Messages.ui.confuseSetting);//$NON-NLS-1$

		tabPane.add(new JScrollPane(resultArea), Messages.ui.analyserReport);//$NON-NLS-1$

		
		
		rootPane.add(tabPane, BorderLayout.CENTER);
		JButton ok = new JButton(Messages.ui.ok); //$NON-NLS-1$
		ok.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ReplacerHolderDialog.this.hidden();
			}
		});
		
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout());
		confuseBox.setText(Messages.ui.autoConfuse);//$NON-NLS-1$
		JLabel perfixLabel = new JLabel(Messages.ui.perfix);//$NON-NLS-1$
		perfixLabel.setToolTipText(Messages.ui.perfixTooltip);//$NON-NLS-1$
		panel.add(confuseBox);
		panel.add(perfixLabel);
		panel.add(perfixText);
		panel.add(ok);
		panel.setPreferredSize(new Dimension(200,35));
		//confuseBox.setPreferredSize(new Dimension(60,20));
		//perfixLabel.setPreferredSize(new Dimension(60,20));
		perfixText.setPreferredSize(new Dimension(40,20));
		//ok.setPreferredSize(new Dimension(60,20));
		rootPane.add(panel, BorderLayout.SOUTH);
		this.setRootPane(rootPane);
	}
}
