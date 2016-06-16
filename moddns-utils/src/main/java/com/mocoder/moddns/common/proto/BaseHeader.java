package com.mocoder.moddns.common.proto;

public abstract class BaseHeader {

	public static final String CHARSET_HEADER = "US-ASCII";
	public static final String CHARSET_DEFAULT_OUTPUT = "UTF-8";

	public abstract Enum<DataPack.ProtocolType> getProtocolType();

	public abstract byte[] toData();

	public abstract String getShortDomain();

}
