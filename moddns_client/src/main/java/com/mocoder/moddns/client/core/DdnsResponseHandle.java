package com.mocoder.moddns.client.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Arrays;

import com.mocoder.moddns.client.config.SystemConfig;
import com.mocoder.moddns.client.util.ClientLogger;
import com.mocoder.moddns.common.proto.ConnHeader;
import com.mocoder.moddns.common.proto.DataPack;
import com.mocoder.moddns.common.proto.HttpHeader;

/**
 * 
 * @author yangshuai
 * 
 */
public class DdnsResponseHandle extends Thread {

	private static final ClientLogger log = ClientLogger.getInstance(DdnsResponseHandle.class);

	private OutputStream ddnsClientOut;
	private Socket localClientSocket;
	private Long connectionId;

	public DdnsResponseHandle(OutputStream ddnsClientOut, Socket localClientSocket, Long connectionId) {
		this.ddnsClientOut = ddnsClientOut;
		this.localClientSocket = localClientSocket;
		this.connectionId = connectionId;
	}

	@Override
	public void run() {

		InputStream localInput = null;
		try {
			localInput = localClientSocket.getInputStream();
		} catch (IOException e) {
			// *从ddns服务器收到关闭命令不需要通知ddns服务器
			if (e.getClass().equals(SocketException.class)) {
				if (Context.localClientSocketMap.get(connectionId) != null && Context.localClientSocketMap.get(connectionId).get("closeAuto") != null) {
					Boolean auto = (Boolean) Context.localClientSocketMap.get(connectionId).get("closeAuto");
					if (auto) {
						log.info("连接[" + connectionId + "]:已执行ddns服务器关闭命令,正常退出");
						synchronized (Context.localClientSocketMap) {
							Context.localClientSocketMap.remove(connectionId);
							log.info("连接[" + connectionId + "]:已从连接池移除");
						}
						return;
					}
				}
				if (localClientSocket.isClosed()) {
					log.info("连接[" + connectionId + "]:本地服务器已经关闭连接,正常退出");
					synchronized (Context.localClientSocketMap) {
						Context.localClientSocketMap.remove(connectionId);
						log.info("连接[" + connectionId + "]:已从连接池移除");
					}
					return;
				}
			}
			log.error("连接[" + connectionId + "]:本地服务器连接获取输入流失败", e);
			try {
				DataPack resp = new DataPack();
				resp.setConnid(connectionId);
				resp.setType(DataPack.ProtocolType.CONN);
				resp.setData(new ConnHeader(connectionId, ConnHeader.CommandType.CLOSE, "local server close connection").toData());
				synchronized (ddnsClientOut) {
					ddnsClientOut.write(resp.toPackData(SystemConfig.getBufferSize() + DataPack.HEADER_LENGTH));
					ddnsClientOut.flush();
				}
			} catch (Exception e2) {
				log.error("连接[" + connectionId + "]:向远程ddns服务器发送关闭通知失败", e2);
			}
			synchronized (Context.localClientSocketMap) {
				Context.localClientSocketMap.remove(connectionId);
				log.info("连接[" + connectionId + "]:已从连接池移除");
			}
			return;
		}

		while (!localClientSocket.isClosed() && ClientManager.isStarted() && !ClientManager.isExit()) {
			byte[] buffer = new byte[SystemConfig.getBufferSize()];
			int readed = 0;
			try {
				readed = localInput.read(buffer);
			} catch (IOException e) {
				// *从ddns服务器收到关闭命令不需要通知ddns服务器
				if (e.getClass().equals(SocketException.class)) {
					if (Context.localClientSocketMap.get(connectionId) != null && Context.localClientSocketMap.get(connectionId).get("closeAuto") != null) {
						Boolean auto = (Boolean) Context.localClientSocketMap.get(connectionId).get("closeAuto");
						if (auto) {
							log.info("连接[" + connectionId + "]:已执行ddns服务器关闭命令,正常退出");
							synchronized (Context.localClientSocketMap) {
								Context.localClientSocketMap.remove(connectionId);
								log.info("连接[" + connectionId + "]:已从连接池移除");
							}
							return;
						}
					}
					if (localClientSocket.isClosed()) {
						log.info("连接[" + connectionId + "]:本地服务器已经关闭连接,正常退出");
						synchronized (Context.localClientSocketMap) {
							Context.localClientSocketMap.remove(connectionId);
							log.info("连接[" + connectionId + "]:已从连接池移除");
						}
						return;
					}
				}
				if (e.getClass().equals(SocketTimeoutException.class)) {
					log.info("连接[" + connectionId + "]:本地服务器连接过期，正常断开");
				} else {
					log.error("连接[" + connectionId + "]:从本地服务器获取内容失败", e);
				}

				if (ClientManager.isStarted() && !ClientManager.isExit()) {
					DataPack dataPack = new DataPack();
					dataPack.setConnid(connectionId);
					dataPack.setType(DataPack.ProtocolType.CONN);
					dataPack.setData(new ConnHeader(connectionId, ConnHeader.CommandType.CLOSE, "local server close connection").toData());
					try {
						synchronized (ddnsClientOut) {
							ddnsClientOut.write(dataPack.toPackData(SystemConfig.getBufferSize() + DataPack.HEADER_LENGTH));
							ddnsClientOut.flush();
						}
						localClientSocket.close();
					} catch (Exception e1) {
						log.error("向远程ddns服务器发送连接命令失败", e);
					}
				}
				synchronized (Context.localClientSocketMap) {
					Context.localClientSocketMap.remove(connectionId);
					log.info("连接[" + connectionId + "]:已从连接池移除");
				}
				return;
			}
			if (readed == -1 && ClientManager.isStarted() && !ClientManager.isExit()) {
				// 本地服务器链接已经关闭，通知ddns关闭http链接
				log.info("连接[" + connectionId + "]:本地服务器链接已经关闭，通知ddns关闭http链接");
				DataPack dataPack = new DataPack();
				dataPack.setConnid(connectionId);
				dataPack.setType(DataPack.ProtocolType.CONN);
				dataPack.setData(new ConnHeader(connectionId, ConnHeader.CommandType.CLOSE, "local server close connection").toData());
				try {
					synchronized (ddnsClientOut) {
						ddnsClientOut.write(dataPack.toPackData(SystemConfig.getBufferSize() + DataPack.HEADER_LENGTH));
						ddnsClientOut.flush();
					}
					localClientSocket.close();
				} catch (Exception e) {
					log.error("向远程ddns服务器发送连接命令失败", e);
				}
				synchronized (Context.localClientSocketMap) {
					Context.localClientSocketMap.remove(connectionId);
					log.info("连接[" + connectionId + "]:已从连接池移除");
				}
				return;
			}
			DataPack dataPack = new DataPack();
			dataPack.setConnid(this.connectionId);
			dataPack.setType(DataPack.ProtocolType.HTTP);
			byte[] newData = Arrays.copyOf(buffer, readed);
			// http头则替换重定向
			if (HttpHeader.beginWithHttpResponseHeader(newData)) {
				HttpHeader httpHeader = new HttpHeader(newData);
				String host = (String) Context.localClientSocketMap.get(connectionId).get("host");
				if (httpHeader.getHeaderString().indexOf(SystemConfig.getLocalServerHost()) != -1) {
					httpHeader.replacedHeaderValue(SystemConfig.getLocalServerHost() + ":" + SystemConfig.getLocalServerPort(), host);
					httpHeader.replacedHeaderValue(SystemConfig.getLocalServerHost(), host);
					dataPack.setData(httpHeader.toData());
				} else {
					dataPack.setData(newData);
				}
				log.info("连接[" + connectionId + "]返回状态头：" + httpHeader.getProtocalHeader());
			} else {
				dataPack.setData(newData);
			}
			try {
				synchronized (ddnsClientOut) {
					ddnsClientOut.write(dataPack.toPackData(SystemConfig.getBufferSize() + DataPack.HEADER_LENGTH));
					ddnsClientOut.flush();
				}
			} catch (IOException e) {
				if (ClientManager.isStarted() && !ClientManager.isExit()) {
					log.error("向远程ddns服务器返回响应数据失败", e);
				}
			}

		}
		if (!localClientSocket.isClosed()) {
			try {
				localClientSocket.close();
				synchronized (Context.localClientSocketMap) {
					Context.localClientSocketMap.remove(connectionId);
					log.info("连接[" + connectionId + "]:已从连接池移除");
				}
			} catch (Exception e) {
			}
		}

	}
}
