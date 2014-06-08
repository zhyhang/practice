package com.zyh.test.j2se.clone;

/**
 * Check transient field whether cloned when shallow clone. Conclusion: no!
 * @author zhyhang
 *
 */
public class TransientClone implements Cloneable {
	

	private transient String hello;
	
	public void setHello(String hello) {
		this.hello = hello;
	}


	@Override
	public String toString() {
		return hello;
	}


	public static void main(String[] args) throws Exception {
		TransientClone org=new TransientClone();
		org.setHello("hello world!");
		TransientClone cloned=(TransientClone) org.clone();
		System.out.println("org:"+org);
		System.out.println("cloned:"+cloned);
	}
	

}
