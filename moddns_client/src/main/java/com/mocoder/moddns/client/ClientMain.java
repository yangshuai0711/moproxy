package com.mocoder.moddns.client;

import com.mocoder.moddns.client.config.SystemConfig;
import com.mocoder.moddns.client.core.ClientManager;
import com.mocoder.moddns.client.gui.GuiAdapter;
import com.mocoder.moddns.client.gui.MainFrame;

import java.io.IOException;

public class ClientMain {

	public static void main(String[] args) {

		String msg = "invalid parameter,you can take one of parameters below(无效的参数，你可以用以下参数):\n-console\n-gui\nexample:\njava -jar moddns_client.jar -console\njavaw -jar moddns_client.jar -gui";
		if (args != null && args.length == 1) {
			if ("-console".equalsIgnoreCase(args[0])) {

				try {
					SystemConfig.init();
					ClientManager.start();
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else if ("-gui".equalsIgnoreCase(args[0])) {
				java.awt.EventQueue.invokeLater(new Runnable() {
					public void run() {
						new GuiAdapter(new MainFrame());
					}
				});
			} else {
				System.err.println(msg);
			}
		} else {
			System.err.println(msg);
			java.awt.EventQueue.invokeLater(new Runnable() {
				public void run() {
					new GuiAdapter(new MainFrame());
				}
			});
			// ClientManager.start();
		}
	}
}
