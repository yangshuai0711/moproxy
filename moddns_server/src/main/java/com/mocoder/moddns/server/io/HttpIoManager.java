package com.mocoder.moddns.server.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class HttpIoManager {
	private Socket connection;
	private OutputStream out;
	private InputStream in;

	public OutputStream getOut() {
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

	public InputStream getIn() {
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

}
