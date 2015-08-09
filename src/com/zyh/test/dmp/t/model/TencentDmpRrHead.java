/**
 * 
 */
package com.zyh.test.dmp.t.model;

import java.io.Serializable;

/**
 * @author zhyhang
 *
 */
public class TencentDmpRrHead implements Serializable {

	public static final int HEAD_LENGTH = Short.BYTES * 4 + Integer.BYTES * 4;

	private static final long serialVersionUID = 4633891053922669554L;

	private static final String SIGNATURE_TOKEN = "tencent_dmp_data_service_key_pinyou";

	// 接口协议版本号，当前为1，unsigned short 必填
	private short version = 1;

	// 命令号，当前为10，unsigned short 必填
	private short command = 10;

	// 业务id，当前为100，unsigned long 必填
	private int bizId = 100;

	// DSP_ID，unsigned long，必填
	private int dspId = 110016;

	// 签名信息，暂时用HashString(GetSigStr(key))得到
	private int signature = calcSignature();

	// 预留字段
	private int reserved = 0;

	// 调用结果，返回包填写,0表示调用成功，1为包检验失败
	private short result;

	// 整个包长度，unsigned short， 必填
	private short length;

	private int calcSignature() {
		StringBuilder sb = new StringBuilder(Integer.toUnsignedString(this.bizId));
		sb.append(Integer.toUnsignedString(dspId)).append(SIGNATURE_TOKEN);
		return hash(sb.toString());
	}

	private int hash(String str) {
		int seed = 131;
		int hash = str.codePoints().reduce((hashing, asc) -> {
			return hashing * seed + asc;
		}).getAsInt();
		return (hash & 0x7FFFFFFF);
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append('\n').append("version:").append(getVersion());
		sb.append('\n').append("command:").append(getCommand());
		sb.append('\n').append("biz_id:").append(getBizId());
		sb.append('\n').append("dsp_id:").append(getDspId());
		sb.append('\n').append("signature:").append(getSignature());
		sb.append('\n').append("reserved:").append(getReserved());
		sb.append('\n').append("result:").append(getResult());
		sb.append('\n').append("length:").append(getLength());
		sb.append('\n');
		return sb.toString();
	}

	public short getVersion() {
		return version;
	}

	public void setVersion(short version) {
		this.version = version;
	}

	public short getCommand() {
		return command;
	}

	public void setCommand(short command) {
		this.command = command;
	}

	public int getBizId() {
		return bizId;
	}

	public void setBizId(int bizId) {
		this.bizId = bizId;
	}

	public int getDspId() {
		return dspId;
	}

	public void setDspId(int dspId) {
		this.dspId = dspId;
	}

	public int getSignature() {
		return signature;
	}

	public void setSignature(int signature) {
		this.signature = signature;
	}

	public int getReserved() {
		return reserved;
	}

	public void setReserved(int reserved) {
		this.reserved = reserved;
	}

	public short getResult() {
		return result;
	}

	public void setResult(short result) {
		this.result = result;
	}

	public short getLength() {
		return length;
	}

	public void setLength(short length) {
		this.length = length;
	}

}
