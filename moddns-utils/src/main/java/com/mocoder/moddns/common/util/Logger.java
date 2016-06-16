package com.mocoder.moddns.common.util;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {

	public enum LogLevel {
		DEBUG(4), INFO(3), WARN(2), ERROR(1), NONE(0);
		private int level;

		LogLevel(int level) {
			this.level = level;
		}

		public int value() {
			return level;
		}
	}

	private static PrintStream console;
	private static PrintStream file;
	private static LogLevel logLevel;
	private static SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm:ss");
	private static Logger impl;
	private Class<?> claz;
	private final Object lock = new Object();

	private Logger() {
	}

	public static <T> Logger getInstance(Class<T> claz) {
		Logger logger = new Logger();
		logger.claz = claz;
		return logger;
	}

	public static void init(LogLevel level, String filePath, String implClassName) {
		if (logLevel != null && console != null && file != null) {
			return;
		}
		if (implClassName != null && implClassName.trim().length() > 1) {
			try {
				impl = Class.forName(implClassName).asSubclass(Logger.class).newInstance();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		logLevel = level;
		console = System.out;
		try {
			file = new PrintStream(new FileOutputStream(filePath, false), false);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public static void init(LogLevel level, String filePath) {
		init(level, filePath, null);
	}

	public String getLogHeader() {
		return sdf.format(new Date()) + " @" + claz.getSimpleName() + " >>> ";
	}

	private void print(String str) {
		synchronized (lock) {
			console.println(getLogHeader() + str);
			file.println(getLogHeader() + str);
		}
	}

	public void debug(String str) {
		if (logLevel.value() >= LogLevel.DEBUG.value()) {
			print(str);
		}
	}

	public void info(String str) {
		if (logLevel.value() >= LogLevel.INFO.value()) {
			print(str);
		}
	}

	public void warn(String str) {
		if (logLevel.value() >= LogLevel.WARN.value()) {
			print(str);
		}
	}

	public void error(String str) {
		if (logLevel.value() >= LogLevel.ERROR.value()) {
			print(str);
		}
	}

	public void error(String str, Exception e) {
		if (logLevel.value() >= LogLevel.ERROR.value()) {
			StringBuilder sb = new StringBuilder(str);
			if (e != null) {
				sb.append("\n").append(e.getClass().getSimpleName() + ":" + e.getMessage()).append("\ncause:").append(e.getCause());
				for (StackTraceElement trace : e.getStackTrace()) {
					sb.append("\n").append(trace.toString());
				}
			}
			print(sb.toString());
		}
	}
}
