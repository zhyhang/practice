/**
 * 
 */
package com.zyh.test.concurrent;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * @author zhyhang
 *
 */
public class ConcurrentSetTry {

	public static void main(String[] args) throws Exception {
		Comparator<? super String> comparator = (a, b) -> {
			int c = Long.compare(Long.valueOf(a.substring(1)), Long.valueOf(b.substring(1)));
			return c != 0 ? c : a.compareTo(b);
		};
		ConcurrentSkipListSet<String> csls1 = new ConcurrentSkipListSet<>(comparator);
		csls1.add("s11");
		csls1.add("s2");
		csls1.add("s02");
		csls1.add("s3");
		csls1.add("s03");
		System.out.println("all set:" + csls1);
		System.out.println("sub set:" + csls1.subSet("s2", "s11"));
		
		Method methodSize1 = ConcurrentSkipListSet.class.getDeclaredMethod("size");
		Method methodSize2 = ConcurrentSkipListSet.class.getDeclaredMethod("size");
		
		Field f1 = ConcurrentSkipListSet.class.getDeclaredField("m");
		f1.setAccessible(true);
		Field f2 = ConcurrentSkipListSet.class.getDeclaredField("m");
		f2.setAccessible(true);
		
		System.out.println("size:"+methodSize1.invoke(csls1));
		System.out.println("size:"+methodSize2.invoke(csls1));
			
		System.out.println("f1:"+f1.get(csls1));
		System.out.println("f2:"+f2.get(csls1));
		
//		System.out.println(ConcurrentSkipListSet.class.getDeclaredMethod("size")+","+System.identityHashCode(ConcurrentSkipListSet.class.getDeclaredMethod("size")));
//		System.out.println(ConcurrentSkipListSet.class.getDeclaredMethod("size")+","+System.identityHashCode(ConcurrentSkipListSet.class.getDeclaredMethod("size")));
	}

}
