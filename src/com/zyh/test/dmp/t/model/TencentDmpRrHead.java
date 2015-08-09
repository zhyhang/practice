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

	// �ӿ�Э��汾�ţ���ǰΪ1��unsigned short ����
	private short version = 1;

	// ����ţ���ǰΪ10��unsigned short ����
	private short command = 10;

	// ҵ��id����ǰΪ100��unsigned long ����
	private int bizId = 100;

	// DSP_ID��unsigned long������
	private int dspId = 110016;

	// ǩ����Ϣ����ʱ��HashString(GetSigStr(key))�õ�
	private int signature = calcSignature();

	// Ԥ���ֶ�
	private int reserved = 0;

	// ���ý�������ذ���д,0��ʾ���óɹ���1Ϊ������ʧ��
	private short result;

	// ���������ȣ�unsigned short�� ����
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
