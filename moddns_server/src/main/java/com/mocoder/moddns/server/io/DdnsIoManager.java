package com.mocoder.moddns.server.io;

import com.mocoder.moddns.common.proto.DataPack;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class DdnsIoManager {

	private Socket connection;
	private Date lastVisitTime;
	private Map<Long, Object> httpConnsMap = new HashMap<Long, Object>();
	private int errorCount;
	private OutputStream out;
	private InputStream in;
	private byte[] buffer;

	private DdnsIoManager(Socket connection, int bufferSize) {
		this.connection = connection;
		buffer = new byte[bufferSize];
		lastVisitTime = new Date();
	}

	public Date getLastVisitTime() {
		return lastVisitTime;
	}

	private void updateVisitTime() {
		lastVisitTime = new Date();
	}

	public Map<Long, Object> getHttpConnsMap() {
		return httpConnsMap;
	}

	public int getErrorCount() {
		return errorCount;
	}

	/**
	 * 获取输出流，失败返回null
	 * 
	 * @author yangshuai
	 * @date 2014-7-30 上午10:18:29
	 * @return
	 */
	private OutputStream getOut() {
		if (out == null && connection != null && !connection.isClosed() && !connection.isOutputShutdown()) {
			if (out == null) {
				try {
					return connection.getOutputStream();
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				return out;
			}
		}
		return null;

	}

	/**
	 * 获取输入流，失败返回null
	 * 
	 * @author yangshuai
	 * @date 2014-7-30 上午10:18:29
	 * @return
	 */
	private InputStream getIn() {
		if (out == null && connection != null && !connection.isClosed() && !connection.isInputShutdown()) {
			if (in == null) {
				try {
					return connection.getInputStream();
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				return in;
			}
		}
		return null;
	}

	/**
	 * 支持线程同步
	 * 
	 * @author yangshuai
	 * @date 2014-7-30 上午10:17:42
	 * @param dataPack
	 * @return
	 */
	public boolean write(DataPack dataPack) {
		updateVisitTime();
		OutputStream outputStream = getOut();
		if (outputStream == null) {
			return false;
		}
		synchronized (outputStream) {
			try {
				outputStream.write(dataPack.toPackData(buffer.length));
				return true;
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}
	}

	/**
	 * 支持线程同步
	 * 
	 * @author yangshuai
	 * @date 2014-7-30 上午10:17:42
	 * @return
	 */
	public DataPack read() {
		updateVisitTime();
		InputStream inputStream = getIn();
		if (inputStream == null) {
			return null;
		}
		synchronized (inputStream) {
			try {
				int readed = inputStream.read(buffer);
				return DataPack.parse(buffer, readed);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
}
