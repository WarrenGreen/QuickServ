package com.green.quickserv;
import java.awt.EventQueue;

import javax.swing.JFrame;

import java.awt.GridBagLayout;

import javax.swing.JTabbedPane;

import java.awt.GridBagConstraints;

import javax.swing.JToolBar;
import javax.swing.BoxLayout;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.JButton;
import javax.swing.JMenuBar;
import javax.swing.JTextField;

import java.awt.FlowLayout;

import javax.swing.JLabel;

import java.awt.Insets;

import javax.swing.SwingConstants;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.IOException;


public class Interaction {

	private JFrame frame;
	private JTabbedPane tabbedPane;
	private JPanel FileViewPanel;
	private JPanel panel;
	private JTree tree;
	private JPanel panel_1;
	private JButton button;
	private JPanel statusBarPanel;
	private JButton btnStart;
	private JLabel lblStatusBar;
	private JLabel lblPort;
	private JTextField txtPort;
	
	private static Server serv;
	private Thread t = null;

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
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new BorderLayout(0, 0));
		
		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		frame.getContentPane().add(tabbedPane, BorderLayout.CENTER);
		
		panel = new JPanel();
		tabbedPane.addTab("Status", null, panel, null);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0, 0, 0};
		gbl_panel.rowHeights = new int[]{0, 0};
		gbl_panel.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);
		
		lblPort = new JLabel("Port: ");
		GridBagConstraints gbc_lblPort = new GridBagConstraints();
		gbc_lblPort.insets = new Insets(0, 0, 0, 5);
		gbc_lblPort.anchor = GridBagConstraints.EAST;
		gbc_lblPort.ipady = 5;
		gbc_lblPort.ipadx = 5;
		gbc_lblPort.gridx = 0;
		gbc_lblPort.gridy = 0;
		panel.add(lblPort, gbc_lblPort);
		
		txtPort = new JTextField();
		GridBagConstraints gbc_txtPort = new GridBagConstraints();
		gbc_txtPort.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtPort.gridx = 1;
		gbc_txtPort.gridy = 0;
		panel.add(txtPort, gbc_txtPort);
		txtPort.setColumns(10);
		
		FileViewPanel = new JPanel();
		tabbedPane.addTab("File View", null, FileViewPanel, null);
		FileViewPanel.setLayout(new BorderLayout(0, 0));
		
		tree = new JTree();
		FileViewPanel.add(tree, BorderLayout.CENTER);
		
		panel_1 = new JPanel();
		FileViewPanel.add(panel_1, BorderLayout.EAST);
		
		button = new JButton("");
		panel_1.add(button);
		
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
					serv = new Server(Integer.parseInt(txtPort.getText()));
					//serv.start();
					t = new Thread(serv);
					t.start();
				
					lblStatusBar.setText("Server: running");
					btnStart.setText("Stop");
				}else{
					serv.running = false;
					try {
						serv.s_sock.close();
					} catch (IOException ex) {
						ex.printStackTrace();
					}
					
					lblStatusBar.setText("Server: stopped");
					btnStart.setText("Start");
				}
			}
		});
		statusBarPanel.add(btnStart);

		
	}
	


}
