package com.zyh.test.j2se;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class IteratePerform {

	public static void main(String[] args) {
		int initialCapacity = 1000;

		List<Integer> ints=new ArrayList<>(initialCapacity);
		for (int i = 0; i < initialCapacity; i++) {
			ints.add(i);
		}
		Integer[] is=ints.toArray(new Integer[initialCapacity]);
		List<Integer> ints1=new ArrayList<>(initialCapacity);
		Map<Integer, Integer> amap=new HashMap<Integer, Integer>();
		long ts1=System.nanoTime();
		for (int i = 0; i < initialCapacity; i++) {
//			ints1.add(ints.get(i));
			Integer aint=ints.get(i);
			if(Math.random() * aint.intValue()<100){
//			Integer aint=is[i];
			ints1.add(aint);
			}
//			amap.put(aint, aint);
		}
		long ts2=System.nanoTime();
		System.out.println(ts2-ts1);
		System.out.println(TimeUnit.NANOSECONDS.toMillis(ts2-ts1));
	}

}
