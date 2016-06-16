package com.mocoder.moddns.server.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.mocoder.moddns.common.proto.AuthHeader;
import com.mocoder.moddns.common.proto.BaseHeader;
import com.mocoder.moddns.common.proto.ConnHeader;
import com.mocoder.moddns.common.proto.DataPack;
import com.mocoder.moddns.common.proto.HttpHeader;
import com.mocoder.moddns.common.proto.ConnHeader.CommandType;
import com.mocoder.moddns.common.proto.DataPack.ProtocolType;
import com.mocoder.moddns.common.util.HeaderHelper;
import com.mocoder.moddns.common.util.Logger;
import com.mocoder.moddns.server.config.SystemConfig;

/**
 * a request handler ,also as a thread ,handling both ddns and http requests.<br/>
 * <p>
 * if as a ddns request,it will keep the socket connection and keep reading data
 * and sent it to http request responses.
 * <p/>
 * <p>
 * other requests will be seem as http requests, they will not be closed until
 * the response is finished.
 * </p>
 * 
 * @author yangshuai
 * @date 2014-7-11 下午11:16:16
 */
public class HandleExecutor implements Runnable {

	private static final Map<String, Map<String, Object>> ddnsConnMap = new HashMap<String, Map<String, Object>>();
	private static final Logger log = Logger.getInstance(HandleExecutor.class);

	private Map<Long, Map<String, Object>> httpConnMap = new HashMap<Long, Map<String, Object>>();
	private static Long counter = 0L;
	private String countStr = "";
	private int requestType = 0;
	private Long connectionId;
	private AuthHeader authHeader;
	private BaseHeader dataHeader;

	private byte[] buffer;
	private int readed = 0;

	private Socket socket;
	private Socket ddnsSocket;
	private OutputStream ddnsOutStream;
	private InputStream inStream;
	private OutputStream outStream;

	public HandleExecutor(Socket socket) {
		synchronized (counter) {
			counter = counter == Long.MAX_VALUE ? 1L : counter + 1;
			connectionId = counter;
		}
		countStr = "Thread_" + connectionId + "_";
		this.socket = socket;
		log.info(getLogTitle() + "和客户端连接建立");
	}

	@Override
	public void run() {
		// try {
		try {
			inStream = socket.getInputStream();
			outStream = socket.getOutputStream();
		} catch (IOException e1) {
			log.error(getLogTitle() + "请求读写流获取失败", e1);
			return;
		}

		log.info(getLogTitle() + "开始读取客户端信息");
		byte[] headerBuffer = new byte[SystemConfig.MAX_HTTP_HEADER_SIZE];
		try {
			readed = inStream.read(headerBuffer);
			if (readed == -1) {
				log.info(getLogTitle() + "请求方已经关闭连接");
				socket.close();
				return;
			}
		} catch (IOException e1) {
			log.error(getLogTitle() + "请求读取错误", e1);
			return;
		} catch (Exception e) {
			log.error("未知错误", e);
			return;
		}

		log.info(getLogTitle() + "开始解析协议头...");
		dataHeader = HeaderHelper.getHeaderInstance(Arrays.copyOf(headerBuffer, readed));
		if (dataHeader.getClass().equals(AuthHeader.class)) {
			requestType = 1;
			log.info(getLogTitle() + "用户身份：ddns服务用户");
			ddnsRequestHandle();
			return;
		} else {
			try {
				socket.setSoTimeout(SystemConfig.KEEPALIVE_TIME_OUT);
			} catch (SocketException e1) {
				log.error(getLogTitle() + "该流不支持超时设置", e1);
			}
			requestType = 2;
			log.info(getLogTitle() + "用户身份：http访问用户");
			if (!httpRequestValidate()) {
				return;
			}
			// 如果通过验证则执行后续处理

			log.info(getLogTitle() + "write first part data to ddns client");
			DataPack dataPack = new DataPack();
			dataPack.setConnid(connectionId);
			dataPack.setType(DataPack.ProtocolType.HTTP);
			dataPack.setData(Arrays.copyOf(headerBuffer, readed));
			synchronized (ddnsOutStream) {
				try {
					ddnsOutStream.write(dataPack.toPackData(buffer.length + DataPack.HEADER_LENGTH));
					ddnsOutStream.flush();
				} catch (IOException e) {
					log.error("ddns写出异常", e);
					handleDdnsError(ddnsSocket, socket);
					return;
				} catch (Exception e) {
					log.error("ddns写出未知异常", e);
					handleDdnsError(ddnsSocket, socket);
					return;
				}
			}
			while (true) {
				synchronized (socket) {
					if (socket.isClosed()) {
						log.warn(getLogTitle() + "the http socket has been closed");
						break;
					}
				}
				try {
					readed = inStream.read(buffer);
				} catch (IOException e) {
					if (!e.getClass().equals(SocketException.class) && !e.getClass().equals(SocketTimeoutException.class)) {
						log.error(getLogTitle() + "http请求继续读取异常", e);
					}
					log.info(getLogTitle() + "浏览器连接已经关闭");

					// 已经被关闭则不发通知给ddns客户端(httconnMap的connId只会被命令移除)
					if (httpConnMap.get(connectionId) == null) {
						break;
					}
					if (!socket.isClosed()) {
						try {
							synchronized (socket) {
								socket.close();
							}
						} catch (IOException e1) {
						}
					}
					sentHttpConnCommand("requestclosed");
					break;
				} catch (Exception e) {
					log.error("未知错误", e);
					break;
				}
				if (readed > -1) {
					dataPack.setData(Arrays.copyOf(buffer, readed));
					synchronized (ddnsOutStream) {
						try {
							log.info(getLogTitle() + "write last datas to ddns client");
							ddnsOutStream.write(dataPack.toPackData(buffer.length + DataPack.HEADER_LENGTH));
							ddnsOutStream.flush();
						} catch (IOException e) {
							log.error("ddns写出异常", e);
							handleDdnsError(ddnsSocket, socket);
						} catch (Exception e) {
							log.error("未知错误", e);
						}
					}
				} else if (readed == -1) {
					log.info(getLogTitle() + "浏览器连接已经关闭");
					sentHttpConnCommand("requestclosed");
					break;
				}
			}
		}
	}

	/**
	 * 获取日志前缀
	 * 
	 * @author yangshuai
	 * @date 2014-7-15 上午12:47:11
	 * @return
	 */
	private String getLogTitle() {
		StringBuilder string = new StringBuilder(countStr).append(requestType == 1 ? "ddns" : ((requestType == 2 ? "http" : "....")));
		string.append("_" + (authHeader == null || authHeader.getUserId() == null ? "guest" : authHeader.getUserId()) + "@"
				+ (authHeader == null || authHeader.getDomainName() == null ? "...." : authHeader.getDomainName()) + "-->");
		return string.toString();
	}

	/**
	 * 判断http请求头信息是否正确
	 * 
	 * @author yangshuai
	 * @date 2014-7-14 上午01:02:09
	 * @throws IOException
	 * @throws UnsupportedEncodingException
	 */
	@SuppressWarnings("unchecked")
	private boolean httpRequestValidate() {
		String host = ((HttpHeader) dataHeader).getHeaderValue("Host");
		if (host == null || !host.contains(SystemConfig.DOMAIN)) {
			log.info(getLogTitle() + "请求的域名不正确");
			try {
				outStream.write(HeaderHelper.genHttpTextResponse("您请求的域名不正确"));
				outStream.flush();
				outStream.close();
				return false;
			} catch (IOException e) {
				log.error(getLogTitle() + "http写入错误提示失败", e);
				return false;
			}
		}
		String doName = host.substring(0, host.indexOf('.'));
		authHeader = new AuthHeader(null, null, null, doName, 0);
		Map<String, Object> map;

		map = HandleExecutor.ddnsConnMap.get(doName);
		if (map != null) {
			ddnsSocket = (Socket) map.get("connect");
			try {
				ddnsOutStream = ddnsSocket.getOutputStream();
			} catch (Exception e2) {
				// 空指针或者连接关闭都无法赋值
			}
			if (ddnsOutStream == null) {
				synchronized (HandleExecutor.ddnsConnMap) {
					HandleExecutor.ddnsConnMap.remove(authHeader.getDomainName());
				}
				map = null;
			} else {
				map.put("lastVisit", new Date());
				httpConnMap = (Map<Long, Map<String, Object>>) map.get("httpConnMap");
				synchronized (httpConnMap) {
					Map<String, Object> map2 = new HashMap<String, Object>();
					map2.put("conn", socket);
					synchronized (HandleExecutor.ddnsConnMap) {
						httpConnMap.put(connectionId, map2);
					}
				}
				buffer = new byte[(Integer) map.get("bufferSize")];
			}
		}
		if (map == null) {

			log.info(getLogTitle() + "访问的三级域名不存在或不在线");
			try {
				outStream.write(HeaderHelper.genHttpTextResponse("您访问的三级域名不存在或不在线"));
				outStream.flush();
				outStream.close();
				return false;
			} catch (IOException e) {
				log.error("http访问通知写入失败", e);
				return false;
			}
		}
		log.info(getLogTitle() + "获取ddns用户连接成功");
		return true;
	}

	/**
	 * ddns用户请求信息处理
	 * 
	 * @author yangshuai
	 * @date 2014-7-13 下午11:16:30
	 */
	private void ddnsRequestHandle() {
		authHeader = (AuthHeader) dataHeader;
		// parse login user success if userId is not null
		if (authHeader.getUserId() != null) {
			Map<String, Object> map = HandleExecutor.ddnsConnMap.get(authHeader.getDomainName());
			// if the connection exists who's key is the login personal domain,
			// then shutdown it,and keep the new one.
			if (map != null) {
				Socket s = (Socket) map.get("connect");
				ddnsOutStream = null;
				if (s != null) {
					try {
						ddnsOutStream = s.getOutputStream();
						DataPack dataPack = new DataPack();
						dataPack.setConnid((Long) map.get("connectId"));
						dataPack.setType(ProtocolType.CONN);
						dataPack.setData(new ConnHeader(-1L, CommandType.CLOSE, "relogin").toData());
						ddnsOutStream.write(dataPack.toPackData((Integer) map.get("bufferSize") + DataPack.HEADER_LENGTH));
						ddnsOutStream.flush();
						ddnsOutStream.close();
					} catch (IOException e) {
						log.error("ddns异地登陆通知失败", e);
					} finally {
						synchronized (s) {
							if (!s.isClosed()) {
								try {
									s.close();
								} catch (IOException e) {
									log.error("ddns连接关闭失败", e);
								}
							}
						}
					}
					log.info(getLogTitle() + "ddns user login repeatly，close the last");
				}
			}
			Map<String, Object> record = new HashMap<String, Object>();
			record.put("connect", this.socket);
			record.put("lastVisit", new Date());
			record.put("httpConnMap", httpConnMap);
			record.put("errorCount", 0);
			record.put("connectId", connectionId);
			record.put("bufferSize", authHeader.getBufferSize());
			log.info(getLogTitle() + "登录用户：" + authHeader.toString());
			synchronized (HandleExecutor.ddnsConnMap) {
				HandleExecutor.ddnsConnMap.put(authHeader.getDomainName(), record);
			}
			try {
				outStream.write("登陆成功".getBytes("utf-8"));
				outStream.flush();
				socket.setSoTimeout(SystemConfig.MAX_DDNS_CONNECTION_IDLE);
				socket.setReceiveBufferSize(authHeader.getBufferSize() + DataPack.HEADER_LENGTH);
			} catch (IOException e) {
				log.error("ddns登录结果写入失败", e);
			} catch (Exception e) {
				log.error("ddns登录结果写入失败,未知异常", e);
			}
			log.info(getLogTitle() + "ddns服务用户登录成功");
			ddnsReadLoop();
		} else {
			try {
				outStream.write(("登陆失败:" + ((AuthHeader) dataHeader).getResultMsg()).getBytes("utf-8"));
				outStream.flush();
				outStream.flush();
				outStream.close();
			} catch (IOException e) {
				log.error("ddns登录失败通知失败", e);
			} catch (Exception e) {
				log.error("ddns登录失败通知失败,未知异常", e);
			}
			log.info(getLogTitle() + "ddns服务用户登录失败:" + ((AuthHeader) dataHeader).getResultMsg());
		}
	}

	/**
	 * ddns读取任务处理
	 * 
	 * @author yangshuai
	 * @date 2014-7-31 下午05:46:55
	 */
	private void ddnsReadLoop() {
		byte[] ddnsBuffer = new byte[authHeader.getBufferSize() + DataPack.HEADER_LENGTH];
		int get = 0;
		while (true) {
			try {
				if (socket.isClosed()) {
					synchronized (HandleExecutor.ddnsConnMap) {
						HandleExecutor.ddnsConnMap.remove(authHeader.getDomainName());
					}
					log.info(getLogTitle() + "ddns用户已经关闭连接");
					return;
				}
				get = 0;
				while (get != -1 && get < ddnsBuffer.length) {
					get += inStream.read(ddnsBuffer, get, ddnsBuffer.length - get);
				}
				if (get == -1) {
					socket.close();
					synchronized (HandleExecutor.ddnsConnMap) {
						HandleExecutor.ddnsConnMap.remove(authHeader.getDomainName());
					}
					log.info(getLogTitle() + "ddns用户已经关闭连接");
					return;
				}
			} catch (SocketTimeoutException e) {
				try {
					socket.close();
				} catch (IOException e1) {

				}
				synchronized (HandleExecutor.ddnsConnMap) {
					HandleExecutor.ddnsConnMap.remove(authHeader.getDomainName());
				}

				log.info(getLogTitle() + "超过最大空闲时间，关闭ddns连接");
				return;
			} catch (IOException e) {
				try {
					socket.close();
				} catch (IOException e1) {
				}
				log.error(getLogTitle() + "ddns连接读取异常", e);
				return;
			} catch (Exception e) {
				log.error(getLogTitle() + "ddns连接读取未知异常", e);
				return;
			}
			log.info(getLogTitle() + "size of response data is " + get + ",now parse it");
			DataPack dataPack = null;
			try {
				dataPack = DataPack.parse(ddnsBuffer, get);
			} catch (Exception e) {
				log.error(getLogTitle() + "fail to parse ddns client responsed data", e);
				continue;
			}

			Socket httpSocket = null;
			OutputStream httpOut = null;
			if (httpConnMap.get(dataPack.getConnid()) != null) {
				httpSocket = (Socket) httpConnMap.get(dataPack.getConnid()).get("conn");
			}
			try {
				httpOut = httpSocket.getOutputStream();
			} catch (Exception e2) {
			}
			if (httpOut == null) {
				dataPack = new DataPack();
				dataPack.setConnid(dataPack.getConnid());
				dataPack.setType(DataPack.ProtocolType.CONN);
				dataPack.setData(new ConnHeader(dataPack.getConnid(), ConnHeader.CommandType.CLOSE, "requestclosed").toData());
				try {
					synchronized (outStream) {
						outStream.write(dataPack.toPackData(authHeader.getBufferSize() + DataPack.HEADER_LENGTH));
						outStream.flush();
					}
				} catch (Exception e) {
					try {
						socket.close();
					} catch (IOException e1) {
					}
					log.error("向远程ddns服务器发送连接命令发生错误", e);
					return;
				}
				synchronized (httpConnMap) {
					httpConnMap.remove(dataPack.getConnid());
				}
				continue;
			}
			// 收到客户端http返回
			if (dataPack.getType().equals(DataPack.ProtocolType.HTTP)) {
				try {
					httpOut.write(dataPack.getData());
				} catch (IOException e) {
					log.error(getLogTitle() + "http连接写入异常", e);
					synchronized (httpConnMap) {
						httpConnMap.remove(dataPack.getConnid());
					}
					try {
						synchronized (httpSocket) {
							httpSocket.close();
						}
					} catch (IOException e1) {
						log.error(getLogTitle() + "关闭失败", e1);
					}
					continue;
				} catch (Exception e) {
					log.error(getLogTitle() + "接受http发生未知异常", e);
					try {
						synchronized (httpSocket) {
							httpSocket.close();
						}
					} catch (IOException e1) {
						log.error(getLogTitle() + "关闭失败", e1);
					}
					continue;
				}
				// 收到客户端connection命令
			} else if (dataPack.getType().equals(DataPack.ProtocolType.CONN)) {
				ConnHeader connHeader = ConnHeader.parse(dataPack.getData());
				if (ConnHeader.CommandType.CLOSE.equals(connHeader.getCommandType())) {
					log.info(getLogTitle() + "收到客户端命令http连接关闭");
					try {
						synchronized (httpSocket) {
							httpSocket.close();
						}
						synchronized (httpConnMap) {
							httpConnMap.remove(dataPack.getConnid());
						}
					} catch (Exception e) {
						log.error(getLogTitle() + "关闭http命令发生异常", e);
					}
				}
			}
		}
	}

	/**
	 * 清除超时ddns连接
	 * 
	 * @author yangshuai
	 * @date 2014-7-29 上午12:19:58
	 */
	public static void cleanTimeoutDdnsData() {
		synchronized (HandleExecutor.ddnsConnMap) {
			if (HandleExecutor.ddnsConnMap.isEmpty()) {
				return;
			}
			for (Iterator<Entry<String, Map<String, Object>>> it = HandleExecutor.ddnsConnMap.entrySet().iterator(); it.hasNext();) {
				Entry<String, Map<String, Object>> ent = it.next();
				Socket socket = (Socket) ent.getValue().get("connect");
				Date lastVisit = (Date) ent.getValue().get("lastVisit");
				if (socket == null || socket.isClosed()) {
					log.info("清理任务：连接【" + ent.getKey() + "】已经失效，执行清除");
					it.remove();
				} else if (lastVisit == null || new Date().getTime() - lastVisit.getTime() >= SystemConfig.MAX_DDNS_CONNECTION_IDLE) {
					log.info("清理任务：连接【" + ent.getKey() + "】已经超时，执行清除");
					it.remove();
				}
			}
		}
	}

	/**
	 * 累计处理ddns连接致命错误
	 * 
	 * @author yangshuai
	 * @date 2014-7-29 上午12:03:36
	 * @param dSocket
	 * @param hSocket
	 */
	public void handleDdnsError(Socket dSocket, Socket hSocket) {
		try {
			synchronized (hSocket) {
				hSocket.close();
			}
		} catch (Exception e) {
			log.error(getLogTitle() + "关闭http连接失败", e);
		}
		if (!ddnsSocket.isClosed()) {
			try {
				dSocket.close();
			} catch (Exception e) {
				log.error(getLogTitle() + "ddns客户端错误：关闭ddns连接失败", e);
			}

		}
	}

	/**
	 * 关闭ddns客户端对应http连接
	 * 
	 * @author yangshuai
	 * @date 2014-8-4 下午03:33:59
	 * @param msg
	 *            原因
	 */
	private void sentHttpConnCommand(String reason) {
		DataPack dataPack = new DataPack();
		dataPack.setConnid(connectionId);
		dataPack.setType(DataPack.ProtocolType.CONN);
		dataPack.setData(new ConnHeader(connectionId, ConnHeader.CommandType.CLOSE, reason).toData());
		try {
			synchronized (ddnsOutStream) {
				if (!ddnsSocket.isClosed()) {
					ddnsOutStream.write(dataPack.toPackData(buffer.length + DataPack.HEADER_LENGTH));
					ddnsOutStream.flush();
				}
			}
		} catch (Exception e1) {
			log.error(getLogTitle() + "向ddns客户端发送连接命令失败", e1);
		}
	}

}
