package com.zyh.test.j2se.performace.iterator;

import java.util.Arrays;
import java.util.List;

public class ArrayListIterate {

	public static void main(String[] args) {
		String[] small = new String[64];
		Arrays.fill(small, "py");
		String[] middle = new String[2048];
		Arrays.fill(middle, "py");
		String[] big = new String[20480];
		Arrays.fill(big, "py");
		iterate(Arrays.asList(small));
		iterate(Arrays.asList(middle));
		iterate(Arrays.asList(big));
	}

	private static void iterate(List<String> list) {
		long ts1 = System.nanoTime();
		for (int i = 0; i < list.size(); i++) {
			String str = list.get(i);
		}
		long ts2 = System.nanoTime();
		System.out.println("size: [" + list.size() + "]; cost time:");
		System.out.println(ts2 - ts1);
	}

}
