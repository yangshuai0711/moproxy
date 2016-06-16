package com.mocoder.moddns.server.core;

import com.mocoder.moddns.common.util.Logger;
import com.mocoder.moddns.server.config.SystemConfig;

/**
 * 定期清理长时间没有请求的用户
 * 
 * @author yangshuai
 * @date 2014-7-14 下午11:42:44
 */
public class CleanOutTask extends Thread {

	private static final Logger log = Logger.getInstance(CleanOutTask.class);

	public CleanOutTask() {
		log.info("清理线程启动成功");
	}

	@Override
	public void run() {

		while (true) {
			try {
				Thread.sleep(SystemConfig.CLEAN_PERIOD);

				HandleExecutor.cleanTimeoutDdnsData();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}
}
