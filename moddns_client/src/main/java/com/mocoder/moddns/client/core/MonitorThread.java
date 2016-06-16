package com.mocoder.moddns.client.core;

import java.net.Socket;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.mocoder.moddns.client.config.SystemConfig;
import com.mocoder.moddns.client.gui.IClientGui;
import com.mocoder.moddns.client.model.MonitorData;
import com.mocoder.moddns.client.util.ClientLogger;

public class MonitorThread extends Thread {
	private static final ClientLogger log = ClientLogger.getInstance(MonitorThread.class);
	private IClientGui gui = ClientManager.getGui();

	@Override
	public void run() {

		while (true) {

			try {
				Thread.sleep(SystemConfig.getMonitor_cycle());
				int tCount = DdnsRquestHandle.getThreadNum();
				// if (tCount > 0) {
				// log.info("连接占用线程总数" + tCount);
				// }
				// StringBuilder notifySb = new StringBuilder();
				// if (tCount > 10) {
				// notifySb.append("并发线程数：" + tCount + "\n");
				// }
				int connCountNow = 0;
				// StringBuilder connSb = new StringBuilder();
				// StringBuilder warnSb = new StringBuilder("失效连接：[");
				int disabledCount = 0;
				Map<Long, Map<String, Object>> map = Context.localClientSocketMap;
				// connSb.append("当前连接组：[");
				synchronized (map) {
					Iterator<Entry<Long, Map<String, Object>>> its = map.entrySet().iterator();
					for (Entry<Long, Map<String, Object>> it; its.hasNext();) {
						it = its.next();
						Map<String, Object> map2 = it.getValue();
						if (map2 != null) {
							Socket socket = (Socket) map2.get("conn");
							if (socket == null || socket.isClosed()) {
								// warnSb.append(it.getKey()).append(",");
								disabledCount++;
							} else {
								// connSb.append(it.getKey().toString() + " ");
								connCountNow++;
							}
						}
					}
				}
				// warnSb.append("]:总计:" + disabledCount);
				// connSb.append("],总数:" + connCountNow);
				// if (connCountNow > 0) {
				// log.info(connSb.toString());
				// }
				// if (disabledCount > 0) {
				// log.warn(warnSb.toString());
				// }
				// if (connCountNow > 10) {
				// notifySb.append("并发连接数:" + connCountNow);
				// }
				MonitorData monitorData = new MonitorData();
				monitorData.setConnectionCount(connCountNow);
				monitorData.setThreadCount(tCount);
				if (gui != null) {
					gui.updateMonitor(monitorData);
				}
			} catch (Exception e) {
				log.error("监控线程异常", e);
			}
		}
	}
}
