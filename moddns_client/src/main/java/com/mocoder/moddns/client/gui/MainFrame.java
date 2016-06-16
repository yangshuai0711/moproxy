/*
 * MainFrame.java
 *
 * Created on __DATE__, __TIME__
 */

package com.mocoder.moddns.client.gui;

import com.mocoder.moddns.client.config.SystemConfig;

/**
 * 
 * @author __USER__
 */
@SuppressWarnings("serial")
public class MainFrame extends javax.swing.JFrame {

	/** Creates new form MainFrame */
	public MainFrame() {
		initComponents();
	}

	// GEN-BEGIN:initComponents
	// <editor-fold defaultstate="collapsed" desc="Generated Code">
	private void initComponents() {

		textPort = new javax.swing.JTextField();
		jLabel5 = new javax.swing.JLabel();
		textHost = new javax.swing.JTextField();
		jLabel4 = new javax.swing.JLabel();
		jLabel6 = new javax.swing.JLabel();
		textUserId = new javax.swing.JTextField();
		jLabel7 = new javax.swing.JLabel();
		textDomain = new javax.swing.JTextField();
		jLabel8 = new javax.swing.JLabel();
		textPassword = new javax.swing.JPasswordField();
		buttonStart = new javax.swing.JButton();
		contentPanel = new javax.swing.JTabbedPane();
		infoPanel = new javax.swing.JPanel();
		infoPanelScroll = new javax.swing.JScrollPane();
		infoPan = new javax.swing.JTextPane();
		buttonInfoStopOut = new javax.swing.JToggleButton();
		buttonInfoAlert = new javax.swing.JToggleButton();
		warnPanel = new javax.swing.JPanel();
		warnPanelScroll = new javax.swing.JScrollPane();
		warnPan = new javax.swing.JTextPane();
		buttonWarnStopOut = new javax.swing.JToggleButton();
		buttonWarnAlert = new javax.swing.JToggleButton();
		errorPanel = new javax.swing.JPanel();
		errorPanelScroll = new javax.swing.JScrollPane();
		errorPan = new javax.swing.JTextPane();
		buttonErrorStopOut = new javax.swing.JToggleButton();
		buttonErrorAlert = new javax.swing.JToggleButton();
		jLabel9 = new javax.swing.JLabel();
		jLabel10 = new javax.swing.JLabel();
		connCountBar = new javax.swing.JProgressBar();
		threadCountBar = new javax.swing.JProgressBar();
		jLabel11 = new javax.swing.JLabel();
		labelRequestAdress = new javax.swing.JLabel();
		buttonToClipbord = new javax.swing.JButton();

		setTitle("MODDNS\u5ba2\u6237\u7aef\u7a0b\u5e8f-ddns.mocoder.com");
		setMinimumSize(new java.awt.Dimension(500, 300));
		setName("Form");
		getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

		textPort.setName("textPort");
		getContentPane().add(textPort, new org.netbeans.lib.awtextra.AbsoluteConstraints(310, 40, 70, -1));

		jLabel5.setFont(new java.awt.Font("微软雅黑", 0, 14));
		jLabel5.setText("\u7aef\u53e3\uff1a");
		jLabel5.setName("jLabel5");
		getContentPane().add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 40, -1, -1));

		textHost.setName("textHost");
		getContentPane().add(textHost, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 40, 170, -1));

		jLabel4.setFont(new java.awt.Font("微软雅黑", 0, 14));
		jLabel4.setText("\u4e3b\u673a\u540d\uff1a");
		jLabel4.setName("jLabel4");
		getContentPane().add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 40, -1, -1));

		jLabel6.setFont(new java.awt.Font("微软雅黑", 0, 14));
		jLabel6.setText("\u7528\u6237\u540d\uff1a");
		jLabel6.setName("jLabel6");
		getContentPane().add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, -1, -1));

		textUserId.setName("textUserId");
		getContentPane().add(textUserId, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 10, 170, -1));

		jLabel7.setFont(new java.awt.Font("微软雅黑", 0, 14));
		jLabel7.setText("\u57df\u540d\uff1a");
		jLabel7.setName("jLabel7");
		getContentPane().add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(470, 10, -1, -1));

		textDomain.setName("textDomain");
		getContentPane().add(textDomain, new org.netbeans.lib.awtextra.AbsoluteConstraints(520, 10, 110, -1));

		jLabel8.setFont(new java.awt.Font("微软雅黑", 0, 14));
		jLabel8.setText("\u5bc6\u7801\uff1a");
		jLabel8.setName("jLabel8");
		getContentPane().add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 10, -1, -1));

		textPassword.setName("textPassword");
		getContentPane().add(textPassword, new org.netbeans.lib.awtextra.AbsoluteConstraints(310, 10, 140, -1));

		buttonStart.setFont(new java.awt.Font("微软雅黑", 0, 14));
		buttonStart.setText("\u542f\u52a8");
		buttonStart.setFocusable(false);
		buttonStart.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
		buttonStart.setName("buttonStart");
		buttonStart.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
		getContentPane().add(buttonStart, new org.netbeans.lib.awtextra.AbsoluteConstraints(410, 40, 220, 80));

		contentPanel.setBackground(new java.awt.Color(255, 255, 255));
		contentPanel.setFont(new java.awt.Font("微软雅黑", 0, 14));
		contentPanel.setName("contentPanel");

		infoPanel.setName("infoPanel");
		infoPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

		infoPanelScroll.setName("infoPanelScroll");

		infoPan.setName("infoPan");
		infoPanelScroll.setViewportView(infoPan);

		infoPanel.add(infoPanelScroll, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 30, 630, 300));

		buttonInfoStopOut.setText("\u6682\u505c\u8f93\u51fa");
		buttonInfoStopOut.setName("buttonInfoStopOut");
		infoPanel.add(buttonInfoStopOut, new org.netbeans.lib.awtextra.AbsoluteConstraints(530, 0, -1, -1));

		buttonInfoAlert.setText("\u81ea\u52a8\u5f39\u51fa");
		buttonInfoAlert.setName("buttonInfoAlert");
		infoPanel.add(buttonInfoAlert, new org.netbeans.lib.awtextra.AbsoluteConstraints(430, 0, -1, -1));

		contentPanel.addTab("\u4fe1\u606f", infoPanel);

		warnPanel.setName("warnPanel");
		warnPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

		warnPanelScroll.setName("warnPanelScroll");

		warnPan.setName("warnPan");
		warnPanelScroll.setViewportView(warnPan);

		warnPanel.add(warnPanelScroll, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 30, 630, 300));

		buttonWarnStopOut.setText("\u6682\u505c\u8f93\u51fa");
		buttonWarnStopOut.setName("buttonWarnStopOut");
		warnPanel.add(buttonWarnStopOut, new org.netbeans.lib.awtextra.AbsoluteConstraints(530, 0, -1, -1));

		buttonWarnAlert.setText("\u81ea\u52a8\u5f39\u51fa");
		buttonWarnAlert.setName("buttonWarnAlert");
		warnPanel.add(buttonWarnAlert, new org.netbeans.lib.awtextra.AbsoluteConstraints(430, 0, -1, -1));

		contentPanel.addTab("\u8b66\u544a", warnPanel);

		errorPanel.setName("errorPanel");
		errorPanel.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

		errorPanelScroll.setName("errorPanelScroll");

		errorPan.setFont(new java.awt.Font("微软雅黑", 0, 14));
		errorPan.setName("errorPan");
		errorPanelScroll.setViewportView(errorPan);

		errorPanel.add(errorPanelScroll, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 30, 630, 300));

		buttonErrorStopOut.setText("\u6682\u505c\u8f93\u51fa");
		buttonErrorStopOut.setName("buttonErrorStopOut");
		errorPanel.add(buttonErrorStopOut, new org.netbeans.lib.awtextra.AbsoluteConstraints(530, 0, -1, -1));

		buttonErrorAlert.setText("\u81ea\u52a8\u5f39\u51fa");
		buttonErrorAlert.setName("buttonErrorAlert");
		errorPanel.add(buttonErrorAlert, new org.netbeans.lib.awtextra.AbsoluteConstraints(430, 0, -1, -1));

		contentPanel.addTab("\u9519\u8bef", errorPanel);

		getContentPane().add(contentPanel, new org.netbeans.lib.awtextra.AbsoluteConstraints(2, 160, 640, 370));

		jLabel9.setFont(new java.awt.Font("微软雅黑", 0, 14));
		jLabel9.setText("\u8fde\u63a5\u6570\uff1a");
		jLabel9.setName("jLabel9");
		getContentPane().add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 70, -1, -1));

		jLabel10.setFont(new java.awt.Font("微软雅黑", 0, 14));
		jLabel10.setText("\u7ebf\u7a0b\u6570\uff1a");
		jLabel10.setName("jLabel10");
		getContentPane().add(jLabel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 100, -1, -1));

		connCountBar.setMaximum(50);
		connCountBar.setToolTipText("");
		connCountBar.setName("connCountBar");
		connCountBar.setString("");
		connCountBar.setStringPainted(true);
		getContentPane().add(connCountBar, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 70, 310, -1));

		threadCountBar.setMaximum(50);
		threadCountBar.setToolTipText("");
		threadCountBar.setName("threadCountBar");
		threadCountBar.setString("");
		threadCountBar.setStringPainted(true);
		getContentPane().add(threadCountBar, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 100, 310, -1));

		jLabel11.setFont(new java.awt.Font("微软雅黑", 0, 14));
		jLabel11.setText("\u8bbf\u95ee\u5730\u5740\uff1a");
		jLabel11.setName("jLabel11");
		getContentPane().add(jLabel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 130, -1, -1));

		labelRequestAdress.setFont(new java.awt.Font("微软雅黑", 0, 14));
		labelRequestAdress.setForeground(new java.awt.Color(255, 0, 0));
		labelRequestAdress.setName("labelRequestAdress");
		getContentPane().add(labelRequestAdress, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 130, 300, 20));

		buttonToClipbord.setText("\u590d\u5236\u5230\u526a\u8d34\u677f");
		getContentPane().add(buttonToClipbord, new org.netbeans.lib.awtextra.AbsoluteConstraints(410, 130, 220, -1));

		pack();
	}

	// GEN-END:initComponents

	public void initValue() {
		textDomain.setText(SystemConfig.getPersonalDomain());
		textHost.setText(SystemConfig.getLocalServerHost());
		textPassword.setText(SystemConfig.getPassword());
		textPort.setText("" + SystemConfig.getLocalServerPort());
		textUserId.setText(SystemConfig.getUserId());
	}

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String args[]) {
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				new MainFrame().setVisible(true);
			}
		});
	}

	// GEN-BEGIN:variables
	// Variables declaration - do not modify
	public javax.swing.JToggleButton buttonErrorAlert;
	public javax.swing.JToggleButton buttonErrorStopOut;
	public javax.swing.JToggleButton buttonInfoAlert;
	public javax.swing.JToggleButton buttonInfoStopOut;
	public javax.swing.JButton buttonStart;
	public javax.swing.JButton buttonToClipbord;
	public javax.swing.JToggleButton buttonWarnAlert;
	public javax.swing.JToggleButton buttonWarnStopOut;
	public javax.swing.JProgressBar connCountBar;
	public javax.swing.JTabbedPane contentPanel;
	public javax.swing.JTextPane errorPan;
	public javax.swing.JPanel errorPanel;
	public javax.swing.JScrollPane errorPanelScroll;
	public javax.swing.JTextPane infoPan;
	public javax.swing.JPanel infoPanel;
	public javax.swing.JScrollPane infoPanelScroll;
	public javax.swing.JLabel jLabel10;
	public javax.swing.JLabel jLabel11;
	public javax.swing.JLabel jLabel4;
	public javax.swing.JLabel jLabel5;
	public javax.swing.JLabel jLabel6;
	public javax.swing.JLabel jLabel7;
	public javax.swing.JLabel jLabel8;
	public javax.swing.JLabel jLabel9;
	public javax.swing.JLabel labelRequestAdress;
	public javax.swing.JTextField textDomain;
	public javax.swing.JTextField textHost;
	public javax.swing.JPasswordField textPassword;
	public javax.swing.JTextField textPort;
	public javax.swing.JTextField textUserId;
	public javax.swing.JProgressBar threadCountBar;
	public javax.swing.JTextPane warnPan;
	public javax.swing.JPanel warnPanel;
	public javax.swing.JScrollPane warnPanelScroll;
	// End of variables declaration//GEN-END:variables
}