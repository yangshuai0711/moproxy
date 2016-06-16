package com.mocoder.moddns.common.proto;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

public class AuthHeader extends BaseHeader {
	private Properties properties = new Properties();

	private String resultMsg;
	private Enum<AuthHeader.Action> action;

	private String userId;
	private String domainName;
	private String password;
	private int bufferSize;

	public AuthHeader(byte[] body) {
		String key = null;
		try {
			key = new String(body, CHARSET_HEADER).trim();
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
			return;
		}
		String[] contents = key.split(DataPack.VALUE_SEP_STRING);
		InputStream input = AuthHeader.class.getClassLoader().getResourceAsStream("users.properties");
		properties.clear();
		try {
			properties.load(input);
			input.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (Action.LOGIN.equals(Action.valueOf(contents[0])) && contents[1].length() > 0 && contents[2].length() > 0 && contents[3].length() > 0) {
			if (properties.containsKey(contents[1])) {
				String pwdAndDomain = (String) properties.get(contents[1]);
				if (pwdAndDomain != null && pwdAndDomain.length() >= 3) {
					String[] pdArr = pwdAndDomain.split(":");
					if (pdArr[0].equals(contents[2]) && pdArr[1].equals(contents[3])) {
						bufferSize = Integer.valueOf(contents[4]);
						if (bufferSize < DataPack.HEADER_LENGTH) {
							resultMsg = "缓存大小设置不正确,请到http://blog.mocoder.com下载最新客户端版本";
							return;
						}
						this.userId = contents[1];
						this.domainName = contents[3];
						this.resultMsg = "成功";
						this.action = Action.LOGIN;
					} else {
						resultMsg = "密码或个性域名不正确";
					}
				} else {
					resultMsg = "密码或个性域名长度不正确";
				}
			} else {
				resultMsg = "不存在此用户ID";
			}

		} else {
			resultMsg = "登陆信息内容不完整";
		}
	}

	public AuthHeader(DataPack dataPack) {
		this(dataPack.getData());
	}

	public AuthHeader(Enum<Action> action, String userId, String password, String domainName, int bufferSize) {
		this.action = action;
		this.userId = userId;
		this.password = password;
		this.domainName = domainName;
		this.bufferSize = bufferSize;
	}

	public Properties getProperties() {
		return properties;
	}

	public Enum<AuthHeader.Action> getAction() {
		return action;
	}

	public String getUserId() {
		return userId;
	}

	public String getDomainName() {
		return domainName;
	}

	public int getBufferSize() {
		return bufferSize;
	}

	/**
	 * 用户解析不成功时为失败原因，成功则为"成功"
	 * 
	 * @author yangshuai
	 * @date 2014-7-18 上午12:01:02
	 * @return
	 */
	public String getResultMsg() {
		return resultMsg;
	}

	@Override
	public Enum<DataPack.ProtocolType> getProtocolType() {
		return DataPack.ProtocolType.AUTH;
	}

	@Override
	public byte[] toData() {
		try {
			return (this.action.toString() + DataPack.VALUE_SEP_STRING + userId + DataPack.VALUE_SEP_STRING + password + DataPack.VALUE_SEP_STRING + domainName + DataPack.VALUE_SEP_STRING + bufferSize)
					.getBytes(CHARSET_HEADER);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static enum Action {
		LOGIN("LOGIN"), LOGOUT("LOGOUT");
		private String action;

		private Action(String action) {
			this.action = action;
		}

		public String toString() {
			return action;
		}
	}

	@Override
	public String getShortDomain() {
		return domainName;
	}

	@Override
	public String toString() {
		return "[action=" + action + ", bufferSize=" + bufferSize + ", domainName=" + domainName + ", resultMsg=" + resultMsg + ", userId=" + userId + "]";
	}

}
