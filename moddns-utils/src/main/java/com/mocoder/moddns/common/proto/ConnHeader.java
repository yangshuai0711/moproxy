package com.mocoder.moddns.common.proto;

import com.mocoder.moddns.common.proto.DataPack.ProtocolType;

import java.io.UnsupportedEncodingException;

public class ConnHeader extends BaseHeader {

	private Long connId;

	private ConnHeader.CommandType commandType;

	private String msg;

	public ConnHeader() {
	}

	public ConnHeader(Long connId, CommandType commandType, String msg) {
		this.connId = connId;
		this.commandType = commandType;
		this.msg = msg;
	}

	public Long getConnId() {
		return connId;
	}

	public void setConnId(Long connId) {
		this.connId = connId;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public ConnHeader.CommandType getCommandType() {
		return commandType;
	}

	public void setCommandType(ConnHeader.CommandType commandType) {
		this.commandType = commandType;
	}

	@Override
	public Enum<ProtocolType> getProtocolType() {
		// TODO Auto-generated method stub
		return DataPack.ProtocolType.CONN;
	}

	@Override
	public byte[] toData() {
		try {
			return (this.connId.toString() + DataPack.VALUE_SEP_STRING + commandType + DataPack.VALUE_SEP_STRING + (msg == null ? "" : msg.toString()))
					.getBytes(BaseHeader.CHARSET_HEADER);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public String getShortDomain() {
		return null;
	}

	public static ConnHeader parse(byte[] data) {
		try {
			String headerString = new String(data, BaseHeader.CHARSET_HEADER);
			String[] headArr = headerString.split(DataPack.VALUE_SEP_STRING);
			ConnHeader conn = new ConnHeader();
			conn.setConnId(Long.valueOf(headArr[0]));
			conn.setCommandType(CommandType.valueOf(headArr[1]));
			conn.setMsg(headArr[2]);
			return conn;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;

	}

	public static enum CommandType {

		OPEN("OPEN"), CLOSE("CLOSE");

		private String type;

		private CommandType(String type) {
			this.type = type;
		}

		public String toString() {
			return type;
		}

	}

}
