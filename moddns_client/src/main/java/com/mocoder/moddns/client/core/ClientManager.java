package com.mocoder.moddns.client.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.mocoder.moddns.client.config.SystemConfig;
import com.mocoder.moddns.client.gui.IClientGui;
import com.mocoder.moddns.client.util.ClientLogger;
import com.mocoder.moddns.common.proto.AuthHeader;
import com.mocoder.moddns.common.proto.DataPack;

public class ClientManager {

	private static final ClientLogger log = ClientLogger.getInstance(ClientManager.class);
	private static boolean started;
	private static boolean exit;
	private static IClientGui gui;
	private static Socket ddnsClientSocket = null;

	public static void start() {

		new Thread(new Runnable() {
			@Override
			public void run() {
				ClientManager.startProcess();
			}
		}).start();

		new Thread(new Runnable() {

			@Override
			public void run() {
				while (true) {
					try {
						Thread.sleep(5000);
						Map<Long, Map<String, Object>> map = Context.localClientSocketMap;
						StringBuilder sBuilder = new StringBuilder("连接清理任务执行,连接列表:[");
						int count = 0;
						synchronized (map) {
							Iterator<Entry<Long, Map<String, Object>>> its = map.entrySet().iterator();
							for (Entry<Long, Map<String, Object>> it; its.hasNext();) {
								it = its.next();
								Map<String, Object> map2 = it.getValue();
								if (map2 != null) {
									Socket socket = (Socket) map2.get("conn");
									if (socket == null || socket.isClosed()) {
										sBuilder.append(it.getKey() + ",");
										count++;
										its.remove();
									}
								}
							}
						}
						sBuilder.append("],总计:" + count);
						if (count > 0) {
							log.info(sBuilder.toString());
						}
					} catch (Exception e) {
						log.error("连接清理任务异常", e);
					}
				}
			}
		}).start();
	}

	public static void init(IClientGui gui) {
		ClientManager.gui = gui;
	}

	private static void startProcess() {
		try {
			log.info("正在连接到远程服务器:" + SystemConfig.getRemote_server_ip() + ":" + SystemConfig.getRemoteServerPort() + "   ...");
			ddnsClientSocket = new Socket(SystemConfig.getRemote_server_ip(), SystemConfig.getRemoteServerPort());
		} catch (UnknownHostException e) {
			log.error("未知的远程服务器", e);
			if (ClientManager.getGui() != null) {
				ClientManager.getGui().alertError("未知的远程服务器");
				ClientManager.getGui().setStartStatusStop();
			}
			return;
		} catch (IOException e) {
			log.error("远程服务器连接失败", e);
			if (ClientManager.getGui() != null) {
				ClientManager.getGui().alertError("远程服务器连接失败");
				ClientManager.getGui().setStartStatusStop();
			}
			return;
		}
		if (ddnsClientSocket.isConnected()) {
			try {
				ddnsClientSocket.setSoTimeout(SystemConfig.getIdleBeforeExit());
			} catch (SocketException e) {

				log.error("您的操作系统不支持超时设置", e);
				try {
					ddnsClientSocket.close();
				} catch (IOException e1) {
					log.error("ddns连接关闭失败", e1);
				}
				if (ClientManager.getGui() != null) {
					ClientManager.getGui().alertError("您的操作系统不支持超时设置");
					ClientManager.getGui().setStartStatusStop();
				}
				return;
			}
			OutputStream ddnsClientOut = null;
			InputStream ddnsClientIn = null;
			int readed = 0;
			byte[] buffer = null;
			try {
				ddnsClientOut = ddnsClientSocket.getOutputStream();
				ddnsClientIn = ddnsClientSocket.getInputStream();
				AuthHeader authorizationHeader = new AuthHeader(AuthHeader.Action.LOGIN, SystemConfig.getUserId(), SystemConfig.getPassword(), SystemConfig.getPersonalDomain(),
						SystemConfig.getBufferSize());
				DataPack dataPack = new DataPack();
				dataPack.setType(DataPack.ProtocolType.AUTH);
				dataPack.setData(authorizationHeader.toData());
				ddnsClientOut.write(dataPack.toPackData(dataPack.getData().length + DataPack.HEADER_LENGTH));
				ddnsClientOut.flush();
				readed = 0;
				buffer = new byte[10240];
				String result = "登陆失败";
				readed = ddnsClientIn.read(buffer);
				if (readed > -1) {
					result = new String(buffer, 0, readed, "utf-8");
				}
				if (!"登陆成功".equals(result)) {
					ddnsClientSocket.close();
					if (ClientManager.getGui() != null) {
						ClientManager.getGui().alertError(result);
						ClientManager.getGui().setStartStatusStop();
					}
					log.error("登录失败,原因：" + result);
					return;
				} else {
					log.info(SystemConfig.getUserId() + ",恭喜你登录成功！！");
					log.info("请打开" + SystemConfig.getPersonalDomain() + "." + SystemConfig.getRemoteServerDomain() + "访问您的网站");
					if (ClientManager.getGui() != null) {
						ClientManager.getGui().alertInfo("恭喜你登录成功\n您的访问地址为：" + SystemConfig.getPersonalDomain() + "." + SystemConfig.getRemoteServerDomain());
						ClientManager.getGui().setStartStatusStart();
					}
				}
				ddnsClientSocket.setSendBufferSize(SystemConfig.getBufferSize() + DataPack.HEADER_LENGTH);
				started = true;
				new DdnsRquestHandle(ddnsClientSocket).run();

			} catch (UnsupportedEncodingException e) {

				log.error("不支持的编码UTF-8", e);
				try {
					ddnsClientSocket.close();
					return;
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				if (ClientManager.getGui() != null) {
					ClientManager.getGui().alertError("您的系统不支持编码UTF-8");
					ClientManager.getGui().setStartStatusStop();
				}
				return;
			} catch (IOException e) {

				log.error("登陆过程异常", e);
				try {
					ddnsClientSocket.close();
				} catch (IOException e1) {
					log.error("ddns连接关闭失败", e1);
				}
				if (ClientManager.getGui() != null) {
					ClientManager.getGui().alertError("未知登录异常");
					ClientManager.getGui().setStartStatusStop();
				}
				return;
			}

		}
	}

	/**
	 * 关闭后其他线程会有回调，通知gui结果
	 * 
	 * @author yangshuai
	 * @date 2014-8-4 下午06:14:41
	 */
	public static void stop() {
		started = false;
		if (ddnsClientSocket != null && !ddnsClientSocket.isClosed()) {
			try {
				ddnsClientSocket.getInputStream().close();
				synchronized (Context.localClientSocketMap) {
					Context.localClientSocketMap.clear();
				}
			} catch (IOException e) {
				log.error("关闭与ddns服务器的连接失败", e);
			}
		} else {
			if (gui != null) {
				gui.setStartStatusStop();
			}
		}
	}

	public static boolean isStarted() {
		return started;
	}

	public static IClientGui getGui() {
		return gui;
	}

	public static boolean isExit() {
		return exit;
	}

	public static void exit() {
		started = false;
		exit = true;
		if (isStarted()) {// 关闭连接后其他线程会回调gui函数关闭界面
			if (ddnsClientSocket != null && !ddnsClientSocket.isClosed()) {
				try {
					ddnsClientSocket.getInputStream().close();
				} catch (IOException e) {
					log.error("关闭与ddns服务器的连接失败", e);
				}
			}
		} else {
			if (gui != null) {
				gui.exit();
			}
		}
	}

}
