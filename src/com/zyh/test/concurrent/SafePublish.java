package com.zyh.test.concurrent;

public class SafePublish {
	private static SafePublish sp=new SafePublish();
	private  Holder h=null;
	
	public Holder getH(){
		return h;
	}
	
	public static void main(String[] args) {
		
		Thread[] ts = new Thread[10];
		System.out.println("Let's go!");
		// use threads
		for (int i = 0; i < ts.length; i++) {
			ts[i] = new Thread(new Runnable() {
				@Override
				public void run() {
					while (true) {
						if (null!=sp.getH()) {
							sp.getH().assertSanity();
//							if(sp.h.getN()!=10000000)
							System.out.println(sp.getH().getN());
						}
					}
				}
			});
			ts[i].start();
		}
		// create threads;
		Thread[] tsc = new Thread[10];
		for (int i = 0; i < tsc.length; i++) {
			tsc[i] = new Thread(new Runnable() {
				@Override
				public void run() {
					while (true) {
						sp.h = new Holder(0);
					}
				}
			});
			tsc[i].start();
		}
		
//		for (int i = 0; i < ts.length; i++) {
//			ts[i].start();
//		}
		

	}

}

/**
 * 多线程访问下，有可能出错，问题不在Holder本身，而在于未正确地发布，可将n声明为final，避免不正确发布
 */
class Holder {
	private int n;

	public Holder(int n) {
		int tn=n;
		for (int i = 0; i < 10000000; i++) {
			tn++;
		}
		this.n = tn;
	}

	public void assertSanity() {
		if (n != n) {
			throw new AssertionError("这两个对象居然不相等？");
		}
	}
	
	public int getN(){
		return n;
	}
}
