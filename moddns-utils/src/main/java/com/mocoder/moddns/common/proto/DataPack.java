package com.mocoder.moddns.common.proto;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import com.mocoder.moddns.common.util.Logger;

public class DataPack {

	private static final Logger log = Logger.getInstance(DataPack.class);

	public static final String PROTOCOL_NAME = "MODDNS";
	public static final String PROTOCOL_VERSION = "1.0";
	public static final String VALUE_SEP_STRING = ":";
	public static final int HEADER_LENGTH = 255;

	private long connid;
	private ProtocolType type;
	private byte[] data;

	public DataPack() {
	}

	/**
	 * 新建数据块
	 * 
	 * @param connid
	 * @param type
	 * @param data
	 */
	public DataPack(long connid, ProtocolType type, byte[] data) {
		this.connid = connid;
		this.type = type;
		this.data = data;
	}

	// 解析数据块
	public static DataPack parse(byte[] alldata) {
		try {
			String header = new String(alldata, BaseHeader.CHARSET_HEADER);
			log.debug("\nparse:totalLength:" + alldata.length + "\n" + header);
			int endIndex = header.indexOf("\r\n");
			if (endIndex < 0) {
				RuntimeException e = new RuntimeException("包协议头找不到结束标志");
				log.error("包协议头找不到结束标志", e);
				throw e;
			}
			header = header.substring(0, endIndex);
			String[] headerArr = header.split(VALUE_SEP_STRING);
			DataPack.ProtocolType type = ProtocolType.valueOf(headerArr[1]);
			long connid = Long.valueOf(headerArr[2]);
			int dataLength = Integer.valueOf(headerArr[3]);
			byte[] data = new byte[dataLength];
			if (dataLength > alldata.length - HEADER_LENGTH) {
				RuntimeException e = new RuntimeException("包数据超长");
				log.error("包数据超长", e);
				throw e;
			}
			System.arraycopy(alldata, endIndex + 2, data, 0, data.length);
			return new DataPack(connid, type, data);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static DataPack parse(byte[] alldata, int length) {
		return parse(Arrays.copyOf(alldata, length));
	}

	public long getConnid() {
		return connid;
	}

	public void setConnid(long connid) {
		this.connid = connid;
	}

	public ProtocolType getType() {
		return type;
	}

	public void setType(ProtocolType type) {
		this.type = type;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public long getDataLength() {
		return data.length;
	}

	public byte[] toPackData(int length) {
		try {
			byte[] protoData = (PROTOCOL_NAME + "/" + PROTOCOL_VERSION + VALUE_SEP_STRING + type.toString() + VALUE_SEP_STRING + connid + VALUE_SEP_STRING + data.length + "\r\n")
					.getBytes(BaseHeader.CHARSET_HEADER);
			if (data == null) {
				log.warn("data=null" + type.toString() + VALUE_SEP_STRING + connid + "\r\n");
			}
			if (data != null && data.length < 1) {// XXX
				log.warn("data<1" + type.toString() + VALUE_SEP_STRING + connid + "\r\n");
			}
			if (length < protoData.length + data.length) {
				RuntimeException e = new RuntimeException("数据总量不能大于输出长度");
				log.error("数据总量不能大于输出长度", e);
				throw e;
			}
			byte[] toreturn = new byte[length];
			System.arraycopy(protoData, 0, toreturn, 0, protoData.length);
			System.arraycopy(data, 0, toreturn, protoData.length, data.length);
			log.debug("\nmessage:totalLength:" + toreturn.length + "\n" + new String(toreturn, BaseHeader.CHARSET_HEADER));// FIXME
			return toreturn;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;

	}

	public static enum ProtocolType {
		HTTP("HTTP"), CONN("CONN"), AUTH("AUTH");
		private String type;

		private ProtocolType(String type) {
			this.type = type;
		}

		public String toString() {
			return type;
		}
	}

}
