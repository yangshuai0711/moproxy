package com.mocoder.moddns.client.core;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class Context {

	public static Map<Long, Map<String, Object>> localClientSocketMap = new HashMap<Long, Map<String, Object>>();

	/**
	 * 关闭所有到本地服务器连接
	 * 
	 * @author yangshuai
	 * @date 2014-7-25 下午07:06:02
	 */
	public static void closeAllLocalConnections() {
		for (java.util.Map.Entry<Long, Map<String, Object>> set : Context.localClientSocketMap.entrySet()) {
			if (set.getValue() != null) {
				synchronized (set.getValue()) {
					if (set.getValue().get("conn") != null && !((Socket) set.getValue().get("conn")).isClosed()) {
						try {
							((Socket) set.getValue().get("conn")).close();
						} catch (IOException e) {
						}
					}
				}
			}
		}
	}
}
