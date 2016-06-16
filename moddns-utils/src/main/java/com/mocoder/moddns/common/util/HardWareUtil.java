package com.mocoder.moddns.common.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 硬件工具类，用户获取本机硬件信息
 * 
 * @author yangshuai
 * @date 2014-6-28 下午05:06:27
 */
public class HardWareUtil {
	private static final String REG_MAC = "\\s([A-F\\d]{2}(:|-)){5}[A-F\\d]{2}$";
	private static final Pattern pattern = Pattern.compile(REG_MAC, Pattern.MULTILINE);


	/**
	 * 获取所有网卡物理地址
	 * @author yangshuai
	 * @date 2014-6-28 下午05:07:04
	 * @return
	 * @return String[]
	 */
	public static String[] getAllMACAddress() {

		Set<String> macsSet = new HashSet<String>();

		String os = System.getProperty("os.name");
		if (os != null) {
			if (os.startsWith("Windows")) {
				try {
					ProcessBuilder pb = new ProcessBuilder("ipconfig", "/all");
					Process p = pb.start();
					BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
					String line = null;
					while ((line = br.readLine()) != null) {
						Matcher result = pattern.matcher(line);
						boolean find = result.find();
						if (!find) {
							continue;
						}
						macsSet.add(result.group().trim());
					}
					br.close();
					return macsSet.toArray(new String[] {});
				} catch (IOException e) {
				}
			} else /* if (os.startsWith("Linux")) */{
				try {
					ProcessBuilder pb = new ProcessBuilder("ifconfig");
					Process p = pb.start();
					BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
					String line;
					while ((line = br.readLine()) != null) {
						Matcher result = pattern.matcher(line);
						boolean find = result.find();
						if (!find) {
							continue;
						}
						macsSet.add(result.group().trim());
					}
					br.close();
					return macsSet.toArray(new String[] {});
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		}
		return null;
	}

	/**
	 * 获取当前机器首个物理地址
	 * @author yangshuai
	 * @date 2014-6-28 下午05:07:32
	 * @return
	 * @return String
	 */
	public static String getFirstMACAddress() {
		String[] macs = getAllMACAddress();
		return macs != null && macs.length != 0 ? macs[0] : null;
	}
}
