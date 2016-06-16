package com.mocoder.moddns.client.util;

import com.mocoder.moddns.client.core.ClientManager;
import com.mocoder.moddns.common.util.Logger;
import com.mocoder.moddns.common.util.Logger.LogLevel;

public class ClientLogger {

	private Logger proxyLogger;

	private ClientLogger() {
	}

	public static <T> ClientLogger getInstance(Class<T> claz) {
		ClientLogger log = new ClientLogger();
		log.proxyLogger = Logger.getInstance(claz);
		return log;
	}

	public void debug(String msg) {
		proxyLogger.debug(msg);
		if (ClientManager.getGui() != null) {
			ClientManager.getGui().appendLogDebug(proxyLogger.getLogHeader() + msg + "\n");
		}
	}

	public void info(String msg) {
		proxyLogger.info(msg);
		if (ClientManager.getGui() != null) {
			ClientManager.getGui().appendLogInfo(proxyLogger.getLogHeader() + msg + "\n");
		}
	}

	public void warn(String msg) {
		proxyLogger.warn(msg);
		if (ClientManager.getGui() != null) {
			ClientManager.getGui().appendLogWarn(proxyLogger.getLogHeader() + msg + "\n");
		}
	}

	public void error(String msg) {
		proxyLogger.error(msg);
		if (ClientManager.getGui() != null) {
			ClientManager.getGui().appendLogError(proxyLogger.getLogHeader() + msg + "\n");
		}
	}

	public void error(String str, Exception e) {
		StringBuilder sb = new StringBuilder(str);
		if (e != null) {
			sb.append("\n").append(e.getClass().getSimpleName() + ":" + e.getMessage()).append("\ncause:").append(e.getCause());
			for (StackTraceElement trace : e.getStackTrace()) {
				sb.append("\n").append(trace.toString());
			}
		}
		error(sb.toString());
	}

	public static void init(LogLevel level, String filePath) {
		Logger.init(level, filePath);
	}
}
