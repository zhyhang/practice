package com.zyh.test.j2se;

public class InnerClassInstance {

	private I i = new I();

	private class I {
		private void print() {
			System.out.print("I'm I");
		}
	}

	public static void main(String... argv) {
		new InnerClassInstance().i.print();
	}

}
