/**
 * 
 */
package com.zyh.test.dmp.t.model;

import java.io.Serializable;

import tencent.ExDMPRequestMsg.ExDMPResponse;

/**
 * @author zhyhang
 *
 */
public class TencentDmpResponse implements Serializable{

	private static final long serialVersionUID = 6038935299121087343L;

	private TencentDmpRrHead head;
	
	private ExDMPResponse body;

	public TencentDmpRrHead getHead() {
		return head;
	}

	public void setHead(TencentDmpRrHead head) {
		this.head = head;
	}

	public ExDMPResponse getBody() {
		return body;
	}

	public void setBody(ExDMPResponse body) {
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
