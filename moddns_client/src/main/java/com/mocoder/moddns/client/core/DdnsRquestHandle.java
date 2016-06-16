package com.mocoder.moddns.client.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import com.mocoder.moddns.client.config.SystemConfig;
import com.mocoder.moddns.client.util.ClientLogger;
import com.mocoder.moddns.common.proto.ConnHeader;
import com.mocoder.moddns.common.proto.ConnHeader.CommandType;
import com.mocoder.moddns.common.proto.DataPack;
import com.mocoder.moddns.common.proto.HttpHeader;

public class DdnsRquestHandle extends Thread {
	private static final ClientLogger log = ClientLogger.getInstance(DdnsRquestHandle.class);

	private Socket ddnsClientSocket;
	private InputStream ddnsClientIn;
	private OutputStream ddnsClientOut;
	private static ExecutorService threadPool = Executors.newCachedThreadPool();

	public DdnsRquestHandle(Socket ddnsClientSocket) {
		this.ddnsClientSocket = ddnsClientSocket;
	}

	public void run() {
		if (ClientManager.getGui() != null) {
			ClientManager.getGui().setStartStatusStart();
		}
		while (ClientManager.isStarted() && !ClientManager.isExit()) {
			if (ddnsClientSocket.isClosed()) {
				log.error("远程连接已断开");
				if (ClientManager.getGui() != null) {
					ClientManager.getGui().alertError("远程ddns连接已断开");
				}
				break;
			}
			if (ddnsClientIn == null || ddnsClientOut == null) {
				try {
					if (ddnsClientIn == null)
						ddnsClientIn = ddnsClientSocket.getInputStream();
					if (ddnsClientOut == null)
						ddnsClientOut = ddnsClientSocket.getOutputStream();
				} catch (IOException e) {
					log.error("远程服务器连接异常，无法获取读写流", e);
					try {
						Context.closeAllLocalConnections();
						ddnsClientIn.close();
					} catch (IOException e2) {

						log.error("关闭服务连接失败,强行退出程序", e2);
					}

					if (ClientManager.getGui() != null) {
						ClientManager.getGui().alertError("远程服务器读写异常");
						ClientManager.getGui().setStartStatusStop();
					}
					return;
				}
			}
			while (true) {
				byte[] buffer = new byte[SystemConfig.getBufferSize() + DataPack.HEADER_LENGTH];
				int readed = 0;
				try {
					while (readed != -1 && readed < buffer.length) {
						readed += ddnsClientIn.read(buffer, readed, buffer.length - readed);
					}
				} catch (SocketTimeoutException e) {
					log.info("\n过长时间无访问，已退出");
					try {
						Context.closeAllLocalConnections();
						ddnsClientIn.close();
					} catch (IOException e2) {
						log.error("关闭服务连接失败,强行退出程序", e2);
					}
					if (ClientManager.getGui() != null) {
						ClientManager.getGui().alertWarn("过长时间无访问，已退出");
						ClientManager.getGui().setStartStatusStop();
					}
					return;
				} catch (IOException e) {
					if (!ClientManager.isStarted() || ClientManager.isExit()) {
						log.info("与ddns服务器连接以手动断开");
						if (ClientManager.getGui() != null) {
							ClientManager.getGui().alertInfo("关闭成功");
							ClientManager.getGui().setStartStatusStop();
							if (ClientManager.isExit()) {
								ClientManager.getGui().exit();
							}
						}
					} else {
						log.error("读取远程ddns数据异常", e);
						if (ClientManager.getGui() != null) {
							ClientManager.getGui().alertError("读取远程ddns数据异常，已退出");
							ClientManager.getGui().setStartStatusStop();
						}
					}
					try {
						Context.closeAllLocalConnections();
						ddnsClientIn.close();
					} catch (IOException e2) {
						log.error("关闭服务连接失败,强行退出程序", e);
					}
					return;
				}
				if (readed == -1) {
					log.error("远程ddns服务器已关闭连接");
					try {
						Context.closeAllLocalConnections();
						ddnsClientIn.close();
					} catch (IOException e2) {
						log.error("关闭服务连接失败,强行退出程序", e2);
					}

					if (ClientManager.getGui() != null) {
						ClientManager.getGui().alertError("远程ddns服务器已关闭连接");
						ClientManager.getGui().setStartStatusStop();
					}

					return;
				}
				if (readed > -1) {
					DataPack dataPack = DataPack.parse(buffer, readed);
					// http请求
					if (dataPack.getType().equals(DataPack.ProtocolType.HTTP)) {
						int result = handleHttp(dataPack);
						if (result == -1) {
							continue;
						} else if (result == -2) {
							return;
						}
						// 链接命令
					} else if (dataPack.getType().equals(DataPack.ProtocolType.CONN)) {
						// 链接命令处理过程
						ConnHeader conn = ConnHeader.parse(dataPack.getData());
						if (CommandType.CLOSE.equals(conn.getCommandType())) {
							if (conn.getConnId() == -1) {
								try {
									Context.closeAllLocalConnections();
									ddnsClientIn.close();
								} catch (IOException e2) {
									log.error("关闭服务连接失败,强行退出程序", e2);
								}
								if (conn.getMsg().equals("relogin")) {
									log.warn("此账号在另一处登录，当前连接[" + dataPack.getConnid() + "]被服务器关闭");
									if (ClientManager.getGui() != null) {
										ClientManager.getGui().alertWarn("检测到您的账号在别处登录，请联系管理员修改密码");
										ClientManager.getGui().setStartStatusStop();
									}
								}
								return;
							} else {
								Socket localClientSocket = null;
								if (Context.localClientSocketMap.get(conn.getConnId()) != null) {
									localClientSocket = (Socket) Context.localClientSocketMap.get(conn.getConnId()).get("conn");
								}
								log.info("连接[" + conn.getConnId() + "]收到ddns服务器关闭连接命令,执行关闭");
								if (localClientSocket != null && !localClientSocket.isClosed()) {
									try {// 自动关闭
										localClientSocket.close();
										if (Context.localClientSocketMap.get(conn.getConnId()) != null) {
											synchronized (localClientSocket) {
												Context.localClientSocketMap.get(conn.getConnId()).put("closeAuto", true);
											}
										}
										log.info("连接[" + conn.getConnId() + "],关闭执行成功");
									} catch (IOException e) {
										log.error("执行ddns关闭http连接命令失败", e);
										log.info("连接[" + conn.getConnId() + "],关闭执行失败");
									}
								} else {
									log.info("连接[" + conn.getConnId() + "],已经关闭，无需执行");
								}
							}
						}
					}

				}
			}
		}

		if (ddnsClientSocket != null && !ddnsClientSocket.isClosed()) {
			try {
				ddnsClientSocket.close();
			} catch (IOException e) {
				log.error("关闭ddns连接失败", e);
			}
		}

		if (ClientManager.getGui() != null) {
			ClientManager.getGui().alertInfo("关闭成功");
			ClientManager.getGui().setStartStatusStop();
			if (ClientManager.isExit()) {
				ClientManager.getGui().exit();
			}
		}
	}

	/**
	 * 获取到本地服务器的新连接
	 * 
	 * @author yangshuai
	 * @date 2014-7-25 下午06:03:59
	 * @return
	 * @throws IOException
	 */
	private Socket getLocalClientSocket() {
		Socket localClientSocket = null;
		try {
			localClientSocket = new Socket(SystemConfig.getLocalServerHost(), SystemConfig.getLocalServerPort());
			localClientSocket.setSoTimeout(SystemConfig.getRequestTimeout());
		} catch (SocketException e) {
			log.error("本地服务器连接异常,请确认本地服务器已就绪");
		} catch (SocketTimeoutException e) {
			log.error("本地服务器连接超时,请确认本地服务器状态正常");
		} catch (Exception e) {
			log.error("本地服务器发生未知异常", e);
		}
		return localClientSocket;
	}

	/**
	 * ddns http请求处理过程
	 * 
	 * @param dataPack
	 * @return -1 跳到下次读取， -2 推出读取，0 正常处理
	 */
	public int handleHttp(DataPack dataPack) {
		String host = null;
		// 如果是头文件，则替换host
		if (HttpHeader.beginWithHttpRequestHeader(dataPack.getData())) {
			HttpHeader header = new HttpHeader(dataPack.getData());
			// // XXX效果有待长时间验证
			// host = header.getHeaderValue("Host");
			// header.replacedHeaderValue(host,
			// SystemConfig.getLocalServerHost() + ":" +
			// SystemConfig.getLocalServerPort());
			// dataPack.setData(header.toData());
			log.info("连接[" + dataPack.getConnid() + "]请求地址：" + header.getRequestPath());
		}
		// 获取本地连接池，不存在或者关闭时则新建并储存
		Socket localClientSocket = null;
		if (Context.localClientSocketMap.get(dataPack.getConnid()) != null) {
			localClientSocket = (Socket) Context.localClientSocketMap.get(dataPack.getConnid()).get("conn");
		}

		// 通过socket的方法不足以判断其是否已经被关闭,通过获取其输出流来判断;
		// 有可能会重新创建已经被关闭的连接，需要在服务器端判断，收到无对应id的连接重新发布关闭命令
		OutputStream localClientOut = null;
		try {
			localClientOut = localClientSocket.getOutputStream();
		} catch (Exception e) {
		}
		if (localClientOut == null) {
			localClientSocket = getLocalClientSocket();
			// 获取失败则跳过
			if (localClientSocket == null) {
				try {
					DataPack resp = new DataPack();
					resp.setConnid(dataPack.getConnid());
					resp.setType(DataPack.ProtocolType.CONN);
					resp.setData(new ConnHeader(dataPack.getConnid(), ConnHeader.CommandType.CLOSE, "local server close connection").toData());
					synchronized (ddnsClientOut) {
						ddnsClientOut.write(resp.toPackData(SystemConfig.getBufferSize() + DataPack.HEADER_LENGTH));
						ddnsClientOut.flush();
					}
					return -1;
				} catch (IOException e1) {
					log.error("连接[" + dataPack.getConnid() + "]:向远程ddns服务器发送关闭链接命令异常", e1);
					try {
						Context.closeAllLocalConnections();
						ddnsClientIn.close();
					} catch (IOException e2) {
						log.error("连接[" + dataPack.getConnid() + "]:关闭远程服务连接失败,强行退出程序", e2);
					}
					if (ClientManager.getGui() != null) {
						ClientManager.getGui().alertError("ddns远程服务器写入异常");
						ClientManager.getGui().setStartStatusStop();
					}
					return -2;
				}
			} else {
				try {
					localClientSocket.setSoTimeout(SystemConfig.getRequestTimeout());
				} catch (Exception e) {
					log.error("连接[" + dataPack.getConnid() + "]：到本地服务器的连接设置超时失败", e);
				}
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("conn", localClientSocket);
				if (host != null) {
					map.put("host", host);
				}
				synchronized (Context.localClientSocketMap) {
					Context.localClientSocketMap.put(dataPack.getConnid(), map);
				}
				threadPool.execute(new DdnsResponseHandle(ddnsClientOut, localClientSocket, dataPack.getConnid()));
			}
		}
		// 获取连接成功时继续处理写入本地服务器
		try {
			if (localClientOut == null) {
				localClientOut = localClientSocket.getOutputStream();
			}
			localClientOut.write(dataPack.getData());
			localClientOut.flush();
		} catch (SocketException e) {
			log.error("连接[" + dataPack.getConnid() + "]:本地服务器连接状态", e);
			try {
				localClientOut.close();
				synchronized (Context.localClientSocketMap) {
					Context.localClientSocketMap.remove(dataPack.getConnid());
				}
			} catch (IOException e1) {
				log.error("连接[" + dataPack.getConnid() + "]:关闭本地服务器连接失败", e1);
			}
		} catch (IOException e) {
			log.error("连接[" + dataPack.getConnid() + "]:写入本地服务器失败", e);
			try {
				Context.closeAllLocalConnections();
				ddnsClientIn.close();
			} catch (IOException e2) {
				log.error("连接[" + dataPack.getConnid() + "]:关闭远程服务连接失败,强行退出程序", e2);
			}
			if (ClientManager.getGui() != null) {
				ClientManager.getGui().alertError("本地服务器写入异常，请检查本地服务器");
				ClientManager.getGui().setStartStatusStop();
			}
			return -2;
		}
		return 0;
	}

	public static int getThreadNum() {
		return ((ThreadPoolExecutor) threadPool).getActiveCount();
	}
}
