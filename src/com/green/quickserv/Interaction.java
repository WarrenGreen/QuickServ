package com.green.quickserv;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;


public class Interaction {

	private JFrame frame;
	private JTabbedPane tabbedPane;
	private JPanel FileViewPanel;
	private JPanel StatusPanel;
	private JTree tree;
	private JPanel panel_1;
	private JButton btnShareAll;
	private JPanel statusBarPanel;
	private JButton btnStart;
	private JLabel lblStatusBar;
	private JLabel lblPort;
	private JTextField txtPort;
	
	private static Server serv;
	private Thread t = null;
	private JButton btnShareNone;
	private JButton btnShare;
	private JButton btnUnshare;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Interaction window = new Interaction();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
	}

	/**
	 * Create the application.
	 */
	public Interaction() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 367, 391);
		frame.setMinimumSize(new Dimension(367, 391));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new BorderLayout(0, 0));
		
		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		frame.getContentPane().add(tabbedPane, BorderLayout.CENTER);
		
		FileViewPanel = new JPanel();
		tabbedPane.addTab("File View", null, FileViewPanel, null);
		
		DefaultMutableTreeNode root = getFileStructure(new DefaultMutableTreeNode(new File("./")));
		tree = new JTree(root);
		CellRenderer cr = new CellRenderer();
		FileViewPanel.setLayout(new BorderLayout(0, 0));
		tree.setCellRenderer(cr);
		
		FileViewPanel.add(tree, BorderLayout.CENTER);
		
		panel_1 = new JPanel();
		FileViewPanel.add(panel_1, BorderLayout.EAST);
		panel_1.setLayout(new GridLayout(10, 1, 0, 0));
		
		btnShareAll = new JButton("Share All");
		btnShareAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setAllShare(true);
			}
		});
		panel_1.add(btnShareAll);
		
		btnShareNone = new JButton("Share None");
		btnShareNone.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setAllShare(false);
			}
		});
		panel_1.add(btnShareNone);
		
		btnUnshare= new JButton("UnShare");
		btnUnshare.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				for(TreePath tp: tree.getSelectionPaths()){
					File f = (File)((DefaultMutableTreeNode)tp.getLastPathComponent()).getUserObject();
					setShare(f, false);
				}
				
				tree.repaint();
			}
		});
		panel_1.add(btnUnshare);
		
		btnShare = new JButton("Share");
		btnShare.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				for(TreePath tp: tree.getSelectionPaths()){
					File f = (File)((DefaultMutableTreeNode)tp.getLastPathComponent()).getUserObject();
					setShare(f, true);
				}
				
				tree.repaint();
			}
		});
		panel_1.add(btnShare);
		
		StatusPanel = new JPanel();
		tabbedPane.addTab("Status", null, StatusPanel, null);
		GridBagLayout gbl_StatusPanel = new GridBagLayout();
		gbl_StatusPanel.columnWidths = new int[]{0, 0, 0};
		gbl_StatusPanel.rowHeights = new int[]{0, 0};
		gbl_StatusPanel.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_StatusPanel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		StatusPanel.setLayout(gbl_StatusPanel);
		
		lblPort = new JLabel("Port: ");
		GridBagConstraints gbc_lblPort = new GridBagConstraints();
		gbc_lblPort.insets = new Insets(0, 0, 0, 5);
		gbc_lblPort.anchor = GridBagConstraints.EAST;
		gbc_lblPort.ipady = 5;
		gbc_lblPort.ipadx = 5;
		gbc_lblPort.gridx = 0;
		gbc_lblPort.gridy = 0;
		StatusPanel.add(lblPort, gbc_lblPort);
		
		txtPort = new JTextField("3047");
		GridBagConstraints gbc_txtPort = new GridBagConstraints();
		gbc_txtPort.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtPort.gridx = 1;
		gbc_txtPort.gridy = 0;
		StatusPanel.add(txtPort, gbc_txtPort);
		txtPort.setColumns(10);
		
		statusBarPanel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) statusBarPanel.getLayout();
		flowLayout.setVgap(0);
		frame.getContentPane().add(statusBarPanel, BorderLayout.NORTH);
		
		lblStatusBar = new JLabel("Status: stopped");
		statusBarPanel.add(lblStatusBar);
		
		btnStart = new JButton("Start");
		btnStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(btnStart.getText().compareTo("Start") == 0){
					if(txtPort.getText().isEmpty() || Integer.parseInt(txtPort.getText()) <= 1024 || Integer.parseInt(txtPort.getText()) > 65535){
						txtPort.setText("Invalid Port");
						return;
					}
					serv = new Server(Integer.parseInt(txtPort.getText()));
					//serv.start();
					t = new Thread(serv);
					t.start();
				
					lblStatusBar.setText("Server: running");
					btnStart.setText("Stop");
					txtPort.setEditable(false);
				}else{
					try {
						serv.s_sock.close();
					} catch (IOException ex) {
						ex.printStackTrace();
					}
					Server.running = false;
					
					lblStatusBar.setText("Server: stopped");
					btnStart.setText("Start");
					txtPort.setEditable(true);
				}
			}
		});
		statusBarPanel.add(btnStart);

		
	}
	
	private void setAllShare(Boolean share){
		Enumeration treeEnum = ((DefaultMutableTreeNode)tree.getModel().getRoot()).preorderEnumeration();
		treeEnum.nextElement();
		while(treeEnum.hasMoreElements()){
			File f = ((File)((DefaultMutableTreeNode)treeEnum.nextElement()).getUserObject());
			setShare(f, share);
		}
		
		tree.repaint();
	}
	
	private void setShare(File f, Boolean share){
		f.setReadable(share);
	}
	
	private DefaultMutableTreeNode getFileStructure(DefaultMutableTreeNode node) {
		File cf = ((File)node.getUserObject());
		if(!cf.isDirectory() || !cf.canRead()) return node;
		for(File f: cf.listFiles()){
			DefaultMutableTreeNode n = new DefaultMutableTreeNode(f);
			node.add(n);
			getFileStructure(n);
		}
		
		return node;
	}
	
	class CellRenderer extends DefaultTreeCellRenderer {
		
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel,   
                boolean expanded, boolean leaf, int row, boolean hasFocus)          
		{  
			super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);  
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;  
			File f = (File) node.getUserObject();			
			
			if(f.canRead()){
				this.setForeground(Color.black);
			}else{
				this.setForeground(Color.red);
			}
			
			return this;  
		}  
		
	}



}
