package com.mocoder.moddns.server.config;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Properties;

public class SystemConfig {

	public static final String DOMAIN;// 主域名，必须
	public static final String LOG_PATH;
	public static final String LOG_LEVEL;
	public static final int PORT;// 端口，必须
	public static final SimpleDateFormat DATE_FORMAT;
	public static final int KEEPALIVE_TIME_OUT;
	public static final int MAX_DDNS_CONNECTION_IDLE;
	public static final int MAX_ERROR_COUNT_BEFORE_CONN_CLOSE;
	public static final int CLEAN_PERIOD;
	public static final int MAX_HTTP_HEADER_SIZE;
	private static final Properties properties;

	static {
		properties = new Properties();
		try {
			properties.load(SystemConfig.class.getClassLoader().getResourceAsStream("config.properties"));
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("配置文件读取失败");
		}
		DOMAIN = properties.getProperty("DOMAIN");
		PORT = Integer.parseInt(properties.getProperty("PORT"));
		DATE_FORMAT = new SimpleDateFormat(properties.getProperty("DATE_FORMAT", "MMdd HHmmss"));
		KEEPALIVE_TIME_OUT = Integer.parseInt(properties.getProperty("KEEPALIVE_TIME_OUT", "5000"));
		MAX_DDNS_CONNECTION_IDLE = Integer.parseInt(properties.getProperty("MAX_DDNS_CONNECTION_IDLE", "600000"));
		MAX_ERROR_COUNT_BEFORE_CONN_CLOSE = Integer.parseInt(properties.getProperty("MAX_ERROR_COUNT_BEFORE_CONN_CLOSE", "5"));
		CLEAN_PERIOD = Integer.parseInt(properties.getProperty("CLEAN_PERIOD", "60000"));
		MAX_HTTP_HEADER_SIZE = Integer.parseInt(properties.getProperty("MAX_HTTP_HEADER_SIZE", "2048"));
		LOG_PATH = properties.getProperty("LOG_PATH");
		LOG_LEVEL = properties.getProperty("LOG_LEVEL");
	}

}
