package com.mocoder.moddns.client.gui;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Component;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Date;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JToggleButton;

import com.mocoder.moddns.client.config.SystemConfig;
import com.mocoder.moddns.client.core.ClientManager;
import com.mocoder.moddns.client.core.MonitorThread;
import com.mocoder.moddns.client.model.MonitorData;

public class GuiAdapter implements IClientGui {

	private static final int MAX_CURRENT_NUM = 30;
	private MainFrame frame;
	private boolean tipshowed;

	private javax.swing.JButton buttonStart;
	private javax.swing.JTabbedPane contentPanel;
	private javax.swing.JTextPane errorPan;
	private javax.swing.JPanel errorPanel;
	private javax.swing.JTextPane infoPan;
	private javax.swing.JPanel infoPanel;
	private javax.swing.JTextField textDomain;
	private javax.swing.JTextField textHost;
	private javax.swing.JPasswordField textPassword;
	private javax.swing.JTextField textPort;
	private javax.swing.JTextField textUserId;
	private javax.swing.JTextPane warnPan;
	private javax.swing.JPanel warnPanel;
	private TrayIcon trayIcon;
	private SystemTray systemTray;
	private JPopupMenu quickMenu;
	private JProgressBar connectionBar;
	private JProgressBar threadBar;
	private JToggleButton buttonErrorAlert;
	private JToggleButton buttonErrorStopOut;
	private JToggleButton buttonInfoAlert;
	private JToggleButton buttonInfoStopOut;
	private JToggleButton buttonWarnAlert;
	private JToggleButton buttonWarnStopOut;
	private JLabel labelUserUrl;
	private JButton buttonCopyToClip;

	public GuiAdapter(final MainFrame frame) {
		this.frame = frame;
		buttonStart = frame.buttonStart;
		contentPanel = frame.contentPanel;
		errorPan = frame.errorPan;
		errorPanel = frame.errorPanel;
		infoPan = frame.infoPan;
		infoPanel = frame.infoPanel;
		textDomain = frame.textDomain;
		textHost = frame.textHost;
		textPassword = frame.textPassword;
		textPort = frame.textPort;
		textUserId = frame.textUserId;
		warnPan = frame.warnPan;
		warnPanel = frame.warnPanel;
		connectionBar = frame.connCountBar;
		threadBar = frame.threadCountBar;
		buttonErrorAlert = frame.buttonErrorAlert;
		buttonErrorStopOut = frame.buttonErrorStopOut;
		buttonInfoAlert = frame.buttonInfoAlert;
		buttonInfoStopOut = frame.buttonInfoStopOut;
		buttonWarnAlert = frame.buttonWarnAlert;
		buttonWarnStopOut = frame.buttonWarnStopOut;
		labelUserUrl = frame.labelRequestAdress;
		buttonCopyToClip = frame.buttonToClipbord;

		buttonErrorAlert.setSelected(true);
		buttonWarnAlert.setSelected(true);
		// 按钮tooltip
		buttonCopyToClip.setToolTipText("点击复制左方网址到剪贴板");
		buttonErrorAlert.setToolTipText("选中时，有新内容会自动切换到此面板");
		buttonErrorStopOut.setToolTipText("选中时，当前面板内容不再继续显示");
		buttonInfoAlert.setToolTipText("选中时，有新内容会自动切换到此面板");
		buttonInfoStopOut.setToolTipText("选中时，当前面板内容不再继续显示");
		buttonWarnAlert.setToolTipText("选中时，有新内容会自动切换到此面板");
		buttonWarnStopOut.setToolTipText("选中时，当前面板内容不再继续显示");

		this.frame.setTitle("魔豆客户端v1.1-blog.mocoder.com");
		// 窗口居中
		frame.setLocationRelativeTo(this.frame.getOwner());
		// 设置图标
		ImageIcon icon = new ImageIcon(DataContainer.logoIconData);
		DataContainer.logoIconData = null;// 回收内存
		frame.setIconImage(icon.getImage());

		// 窗口关闭操作
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				// super.windowClosing(e);
				frame.dispose();
				if (!tipshowed) {
					trayIcon.displayMessage("提示", "程序正在后台运行，\n双击显示主界面,退出请点击右键", MessageType.INFO);
					tipshowed = true;
				}
			}
		});
		// 系统托盘
		if (SystemTray.isSupported()) {
			PopupMenu popupMenu = new PopupMenu("魔豆客户端");
			MenuItem exitMenuItem = new MenuItem("退出(Exit)");
			MenuItem openMenuItem = new MenuItem("打开(Open)");
			exitMenuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int result = JOptionPane.showConfirmDialog(frame, "确定退出客户端?", "确认", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
					if (result == 0) {
						ClientManager.exit();
						systemTray.remove(trayIcon);
					}
				}
			});
			openMenuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					frame.setVisible(true);
					frame.toFront();
				}
			});
			popupMenu.add(openMenuItem);
			popupMenu.add(exitMenuItem);
			trayIcon = new TrayIcon(icon.getImage(), "moddns客户端", popupMenu);
			trayIcon.setImageAutoSize(true);
			trayIcon.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					frame.setVisible(true);
					frame.toFront();
				}
			});
			SystemTray systemTray = SystemTray.getSystemTray();
			try {
				systemTray.add(trayIcon);
			} catch (AWTException e1) {
				alertError("系统托盘添加失败");
				System.exit(0);
			}
		}

		// 日志浏览右键菜单
		quickMenu = new JPopupMenu("右键菜单");
		JMenuItem cleanrItem = new JMenuItem("清空");
		cleanrItem.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				Component select = contentPanel.getSelectedComponent();
				if (select == infoPanel) {
					infoPan.setText("");
				} else if (select == warnPanel) {
					warnPan.setText("");
				} else if (select == errorPanel) {
					errorPan.setText("");
				}
			}
		});
		quickMenu.add(cleanrItem);
		infoPan.setComponentPopupMenu(quickMenu);
		warnPan.setComponentPopupMenu(quickMenu);
		errorPan.setComponentPopupMenu(quickMenu);

		// 启动
		buttonStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (buttonStart.getText().equals("启动")) {
					SystemConfig.setLocalServerHost(textHost.getText());
					SystemConfig.setLocalServerPort(Integer.valueOf(textPort.getText()));
					SystemConfig.setPassword(textPassword.getText());
					SystemConfig.setPersonalDomain(textDomain.getText());
					SystemConfig.setUserId(textUserId.getText());
					try {
						SystemConfig.saveConfig();
					} catch (Exception e1) {
						alertError("配置保存失败，请检查文件读写权限");
						return;
					}
					buttonStart.setEnabled(false);
					setConfigEditable(false);
					infoPan.setText("");
					warnPan.setText("");
					errorPan.setText("");
					ClientManager.start();

				} else {
					buttonStart.setEnabled(false);
					// SystemConfig.saveConfig();
					ClientManager.stop();
				}
			}
		});

		// 复制地址到剪贴板
		buttonCopyToClip.setEnabled(false);
		final Clipboard clipbord = frame.getToolkit().getSystemClipboard();
		buttonCopyToClip.addActionListener(new ActionListener() {

			
			public void actionPerformed(ActionEvent e) {
				try {
					clipbord.setContents(new StringSelection(labelUserUrl.getText()), null);
					JOptionPane.showMessageDialog(frame, "复制成功", "消息", JOptionPane.INFORMATION_MESSAGE);
				} catch (Exception e2) {
					JOptionPane.showMessageDialog(frame, "复制成功", "消息", JOptionPane.INFORMATION_MESSAGE);
				}
			}
		});

		// 初始化表单参数值
		textDomain.setText(SystemConfig.getPersonalDomain());
		textHost.setText(SystemConfig.getLocalServerHost());
		textPassword.setText(SystemConfig.getPassword());
		textPort.setText("" + SystemConfig.getLocalServerPort());
		textUserId.setText(SystemConfig.getUserId());

		frame.setResizable(false);
		frame.setVisible(true);
		try {
			SystemConfig.init();
		} catch (Exception e1) {
			alertError("配置初始化失败，请检查配置文件版本和配置");
			System.exit(0);
		}
		ClientManager.init(this);
		new MonitorThread().start();
	}

	@Override
	public void alertError(String msg) {
		JOptionPane.showMessageDialog(frame, msg, "错误", JOptionPane.ERROR_MESSAGE);
	}

	@Override
	public void alertInfo(String msg) {
		JOptionPane.showMessageDialog(frame, msg, "通知", JOptionPane.INFORMATION_MESSAGE);
	}

	@Override
	public void alertWarn(String msg) {
		JOptionPane.showMessageDialog(frame, msg, "警告", JOptionPane.WARNING_MESSAGE);
	}

	@Override
	public void appendLogDebug(String msg) {
		// XXX debug面板暂时不提供内容
	}

	@Override
	public void appendLogError(String msg) {
		if (buttonErrorAlert.isSelected()) {
			synchronized (contentPanel) {
				contentPanel.setSelectedIndex(2);
			}
		}
		if (!buttonErrorStopOut.isSelected()) {
			synchronized (errorPan) {
				errorPan.setText(errorPan.getText().concat(msg));
			}
		}
	}

	@Override
	public void appendLogInfo(String msg) {
		if (buttonInfoAlert.isSelected()) {
			synchronized (contentPanel) {
				contentPanel.setSelectedIndex(0);
			}
		}
		if (!buttonInfoStopOut.isSelected()) {
			synchronized (infoPan) {
				// contentPanel.setSelectedIndex(0);
				infoPan.setText(infoPan.getText().concat(msg));
			}
		}
	}

	@Override
	public void appendLogWarn(String msg) {
		if (buttonWarnAlert.isSelected()) {
			synchronized (contentPanel) {
				contentPanel.setSelectedIndex(1);
			}
		}
		if (!buttonWarnStopOut.isSelected()) {
			synchronized (contentPanel) {
				warnPan.setText(warnPan.getText().concat(msg));
			}
		}
	}

	@Override
	public String getLocalServerHost() {
		return textHost.getText();
	}

	@Override
	public int getLocalServerPort() {
		return Integer.valueOf(textPort.getText());
	}

	@Override
	public String getPassword() {
		return textPassword.getText();
	}

	@Override
	public String getPersonalDomain() {
		return textDomain.getText();
	}

	@Override
	public String getUserId() {
		return textUserId.getText();
	}

	@Override
	public synchronized void setStartStatusStart() {
		buttonStart.setEnabled(true);
		labelUserUrl.setText("http://" + SystemConfig.getPersonalDomain() + "." + SystemConfig.getRemoteServerDomain());
		buttonCopyToClip.setEnabled(true);
		buttonStart.setText("停止");
	}

	@Override
	public synchronized void setStartStatusStop() {
		buttonStart.setEnabled(true);
		buttonStart.setText("启动");
		labelUserUrl.setText("");
		buttonCopyToClip.setEnabled(false);
		setConfigEditable(true);
	}

	@Override
	public void exit() {
		frame.dispose();
		System.exit(0);
	}

	private void setConfigEditable(boolean enable) {
		textDomain.setEditable(enable);
		textHost.setEditable(enable);
		textPassword.setEditable(enable);
		textPort.setEditable(enable);
		textUserId.setEditable(enable);
	}

	@Override
	public void updateMonitor(MonitorData data) {
		synchronized (connectionBar) {
			connectionBar.setValue(data.getConnectionCount());
			connectionBar.setString(data.getConnectionCount() + "");
		}
		synchronized (threadBar) {
			threadBar.setValue(data.getThreadCount());
			threadBar.setString(data.getThreadCount() + "");
		}

		if (data.getConnectionCount() <= MAX_CURRENT_NUM) {
			synchronized (connectionBar) {
				connectionBar.setForeground(Color.BLUE);
			}
		}
		if (data.getThreadCount() <= MAX_CURRENT_NUM) {
			synchronized (threadBar) {
				threadBar.setForeground(Color.BLUE);
			}
		}
		if (data.getConnectionCount() > MAX_CURRENT_NUM || data.getThreadCount() > MAX_CURRENT_NUM) {
			StringBuilder sb = new StringBuilder();
			if (data.getConnectionCount() > MAX_CURRENT_NUM) {
				sb.append("并发链接数量过高:" + data.getConnectionCount() + "\n");
				synchronized (connectionBar) {
					connectionBar.setForeground(Color.RED);
				}
			}
			if (data.getThreadCount() > MAX_CURRENT_NUM) {
				sb.append("并发线程数量过高:" + data.getThreadCount() + "\n");
				synchronized (threadBar) {
					threadBar.setForeground(Color.RED);
				}
			}

			if (sb.length() > 0) {
				synchronized (trayIcon) {
					if (new Date().getTime() % 1000 != 0) {
						trayIcon.displayMessage("警告", sb.toString(), MessageType.WARNING);
					}
				}
			}

		}
	}
}
