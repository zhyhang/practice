/**
 * 
 */
package com.zyh.test.dmp.t.model;

import java.io.Serializable;

import tencent.ExDMPRequestMsg.ExDMPRequest;

/**
 * @author zhyhang
 *
 */
public class TencentDmpRequest implements Serializable {

	private static final long serialVersionUID = -8273611061436783445L;

	private TencentDmpRrHead head;

	private ExDMPRequest body;

	public TencentDmpRrHead getHead() {
		return head;
	}

	public void setHead(TencentDmpRrHead head) {
		this.head = head;
	}

	public ExDMPRequest getBody() {
		return body;
	}

	public void setBody(ExDMPRequest body) {
		this.body = body;
	}

	public String toString() {
		String to = "";
		if (this.head != null) {
			to += head.toString();
		}
		if (this.body != null) {
			to += body.toString();
		}
		return to;
	}

}
