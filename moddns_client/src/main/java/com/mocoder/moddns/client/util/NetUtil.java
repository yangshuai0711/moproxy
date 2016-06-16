package com.mocoder.moddns.client.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class NetUtil {
	InetAddress myIpAddress = null;
	InetAddress[] myServer = null;

	public static void main(String args[]) {
		NetUtil address = new NetUtil();
		System.out.println("Your host IP is: " + address.getLocalhostIP());

		String domain = "m.kom.hk";
		System.out.println("The server domain name is: " + domain);
		InetAddress[] array = address.getServerIP(domain);
		int count = 0;
		for (int i = 1; i < array.length; i++) {
			System.out.println("ip " + i + " " + address.getServerIP(domain)[i - 1]);
			count++;
		}
		System.out.println("IP address total: " + count);
	}

	/**
	 * 获得 localhost 的IP地址
	 * 
	 * @return
	 */
	public InetAddress getLocalhostIP() {
		try {
			myIpAddress = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return (myIpAddress);
	}

	/**
	 * 获得某域名的IP地址
	 * 
	 * @param domain
	 *            域名
	 * @return
	 */
	public InetAddress[] getServerIP(String domain) {
		try {
			myServer = InetAddress.getAllByName(domain);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return (myServer);
	}

}
