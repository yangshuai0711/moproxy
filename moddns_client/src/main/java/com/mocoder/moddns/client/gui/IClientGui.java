package com.mocoder.moddns.client.gui;

import com.mocoder.moddns.client.model.MonitorData;

public interface IClientGui {

	void setStartStatusStop();

	void setStartStatusStart();

	String getLocalServerHost();

	int getLocalServerPort();

	String getUserId();

	String getPassword();

	String getPersonalDomain();

	void appendLogDebug(String msg);

	void appendLogInfo(String msg);

	void appendLogWarn(String msg);

	void appendLogError(String msg);

	void alertInfo(String msg);

	void alertWarn(String msg);

	void alertError(String msg);

	void exit();

	void updateMonitor(MonitorData msg);
}
