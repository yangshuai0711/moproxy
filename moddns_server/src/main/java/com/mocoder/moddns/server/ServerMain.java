package com.mocoder.moddns.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.mocoder.moddns.common.util.Logger;
import com.mocoder.moddns.common.util.Logger.LogLevel;
import com.mocoder.moddns.server.config.SystemConfig;
import com.mocoder.moddns.server.core.CleanOutTask;
import com.mocoder.moddns.server.core.HandleExecutor;

public class ServerMain {

	private static final Logger log = Logger.getInstance(ServerMain.class);

	public static void startServer() {
		try {
			if (SystemConfig.CLEAN_PERIOD == 0 || SystemConfig.DOMAIN == null || SystemConfig.DOMAIN.length() < 1 || SystemConfig.MAX_DDNS_CONNECTION_IDLE == 0
					|| SystemConfig.MAX_ERROR_COUNT_BEFORE_CONN_CLOSE == 0 || SystemConfig.KEEPALIVE_TIME_OUT == 0) {
				log.error("配置错误");
			}
			ServerSocket server = new ServerSocket(SystemConfig.PORT);
			new CleanOutTask().start();
			ExecutorService pool = Executors.newCachedThreadPool();
			if (server.isBound()) {
				log.info("ddns服务 v1.0 启动成功,端口：" + server.getLocalPort());
			}
			while (true) {
				Socket userSocket = server.accept();
				if (userSocket.isConnected()) {
					pool.execute(new HandleExecutor(userSocket));
					// new Thread(new HandleExecutor(userSocket)).start();
				}
			}
		} catch (IOException e) {
			log.error("ddns服务启动失败", e);
		}
	}

	public static void main(String[] args) {
		Logger.init(LogLevel.valueOf(SystemConfig.LOG_LEVEL.toUpperCase()), SystemConfig.LOG_PATH);
		startServer();
	}
}
