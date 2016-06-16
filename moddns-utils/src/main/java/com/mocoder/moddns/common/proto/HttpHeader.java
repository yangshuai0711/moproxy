package com.mocoder.moddns.common.proto;

import com.mocoder.moddns.common.proto.DataPack.ProtocolType;
import com.mocoder.moddns.common.util.Logger;

import java.io.UnsupportedEncodingException;

/**
 * http头
 * 
 * @author yangshuai
 * @date 2014-7-17 下午11:12:59
 */
public class HttpHeader extends BaseHeader {

	private static final Logger log = Logger.getInstance(HttpHeader.class);

	public static final String HEADER_PROTOCOL_NAME_HTTP = "HTTP";

	private byte[] body;
	private String headerString;
	private String protoHeader;

	/**
	 * 
	 * @param body
	 *            http头数据，可包含数据区
	 */
	public HttpHeader(byte[] body) {
		if (beginWithHttpHeader(body)) {
			try {
				String tep = new String(body, CHARSET_HEADER);
				int end = tep.indexOf("\r\n\r\n");
				if (end != -1) {
					protoHeader = tep.substring(0, tep.indexOf("\r\n"));
					headerString = tep.substring(0, end + 4);
					this.body = new byte[body.length - end - 4];
					System.arraycopy(body, end + 4, this.body, 0, this.body.length);
				} else {
					RuntimeException exception = new RuntimeException("http头没有协议头结束行");
					log.error("httpHeader解析错误", exception);
					throw exception;
				}
			} catch (UnsupportedEncodingException e) {
				log.error("不支持字符集us-ascII", e);
				RuntimeException exception = new RuntimeException("字符集不支持");
				log.error("httpHeader解析错误", exception);
				throw exception;
			}
		} else {
			RuntimeException e = new RuntimeException("不是有效的http头");
			log.error("不是有效的http头", e);
			throw e;
		}

	}

	public HttpHeader(DataPack dataPack) {
		this(dataPack.getData());
	}

	public byte[] getHeader() {
		try {
			return getHeaderString().getBytes(CHARSET_HEADER);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}

	public byte[] getContent() {
		return body;
	}

	/**
	 * 获取http头指定heander属性
	 * @param name
	 * @return
	 */
	public String getHeaderValue(String name) {
		name += ": ";
		int beginIndex = headerString.indexOf("\r\n" + name) + 2;
		if (beginIndex == -1) {
			return null;
		}
		int newLineIndex = headerString.indexOf("\r\n", beginIndex + name.length());
		return headerString.substring(beginIndex + name.length(), newLineIndex).trim();
	}

	public void replacedHeaderValue(String pre, String value) {
		int repIndex = headerString.indexOf(pre);
		if (repIndex != -1) {
			headerString = headerString.replace(pre, value);
		}
	}

	public void setHeaderValue(String attrName, String value) {
		String preValue = getHeaderValue(attrName);
		int repIndex = headerString.indexOf("\r\n" + attrName + ": " + preValue);
		if (repIndex != -1) {
			headerString = headerString.replace("\r\n" + attrName + ": " + preValue, "\r\n" + attrName + ": " + value);
		}
	}

	public String getHeaderString() {
		return headerString;
	}

	@Override
	public byte[] toData() {
		byte[] content = getContent();
		try {
			if (content != null && content.length > 0) {
				byte[] newHeader = headerString.getBytes(CHARSET_HEADER);
				byte[] toreturn = new byte[newHeader.length + content.length];
				System.arraycopy(newHeader, 0, toreturn, 0, newHeader.length);
				System.arraycopy(content, 0, toreturn, newHeader.length, content.length);
				return toreturn;
			} else {
				return getHeader();
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public Enum<ProtocolType> getProtocolType() {
		return DataPack.ProtocolType.HTTP;
	}

	@Override
	public String getShortDomain() {
		String host = getHeaderValue("Host");
		return host.substring(0, host.indexOf('.'));
	}

	public static boolean beginWithHttpHeader(byte[] body) {
		return beginWithHttpRequestHeader(body) || beginWithHttpResponseHeader(body);
	}

	public static boolean beginWithHttpRequestHeader(byte[] body) {
		try {
			String header = new String(body, BaseHeader.CHARSET_HEADER).trim();
			int newLingIndex = header.indexOf("\r\n");
			if (newLingIndex > 4) {
				String[] arr = header.substring(0, newLingIndex).split(" ");
				if (arr.length == 3 && arr[0].length() >= 3 && arr[0].length() <= 7 && arr[1].length() > 0 && arr[2].contains(HEADER_PROTOCOL_NAME_HTTP + "/")) {
					return true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public static boolean beginWithHttpResponseHeader(byte[] body) {
		try {
			String header = new String(body, BaseHeader.CHARSET_HEADER).trim();
			int newLingIndex = header.indexOf("\r\n");
			if (newLingIndex > 4) {
				String[] arr = header.substring(0, newLingIndex).split(" ");
				if (arr.length >= 3 && arr[0].startsWith(HEADER_PROTOCOL_NAME_HTTP + "/") && Integer.valueOf(arr[1]) >= 100 && arr[2].length() > 0) {
					return true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public String getRequestPath() {
		if (beginWithHttpRequestHeader(getHeader())) {
			String[] arr = protoHeader.split(" ");
			if (arr.length == 3 && arr[0].length() >= 3 && arr[0].length() <= 7 && arr[1].length() > 0 && arr[2].contains(HEADER_PROTOCOL_NAME_HTTP + "/")) {
				return getHeaderValue("Host") + arr[1];
			}
		}
		return null;
	}

	public String getProtocalHeader() {
		return this.protoHeader;
	}
}
