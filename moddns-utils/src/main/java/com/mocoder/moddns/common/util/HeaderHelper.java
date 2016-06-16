package com.mocoder.moddns.common.util;

import java.io.UnsupportedEncodingException;

import com.mocoder.moddns.common.proto.AuthHeader;
import com.mocoder.moddns.common.proto.BaseHeader;
import com.mocoder.moddns.common.proto.DataPack;
import com.mocoder.moddns.common.proto.HttpHeader;

public class HeaderHelper {

	public static BaseHeader getHeaderInstance(DataPack dataPack) {
		switch (dataPack.getType()) {
		case HTTP:
			return new HttpHeader(dataPack);
		case CONN:
			return null;
		case AUTH:
			return new AuthHeader(dataPack);
		}
		return null;
	}

	/**
	 * 根据内容解析请求头，如果是ddns协议头则根据对应类型解析头，否则直接解析为http头
	 * 
	 * @author yangshuai
	 * @date 2014-7-24 下午11:24:29
	 * @param body
	 * @return
	 */
	public static BaseHeader getHeaderInstance(byte[] body) {
		try {
			String headerString = new String(body, BaseHeader.CHARSET_HEADER).trim();
			if (headerString.startsWith(DataPack.PROTOCOL_NAME)) {
				return getHeaderInstance(DataPack.parse(body));
			} else if (HttpHeader.beginWithHttpHeader(body)) {
				return new HttpHeader(body);
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static byte[] genHttpTextResponse(String outString) throws UnsupportedEncodingException {
		String headerStr = null;
		try {
			headerStr = ("HTTP/1.1 200 OK\r\n" + "Server: moddns-server/0.1\r\n" + "Content-Type: text/html;charset=" + BaseHeader.CHARSET_DEFAULT_OUTPUT + "\r\n" + "Content-Length: "
					+ outString.getBytes(BaseHeader.CHARSET_DEFAULT_OUTPUT).length + "\r\n" + "\r\n");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		byte[] header = headerStr.getBytes(BaseHeader.CHARSET_HEADER);
		byte[] content = outString.getBytes(BaseHeader.CHARSET_DEFAULT_OUTPUT);
		byte[] toreturn = new byte[header.length + content.length];
		System.arraycopy(header, 0, toreturn, 0, header.length);
		System.arraycopy(content, 0, toreturn, header.length, content.length);
		return toreturn;
	}
}
