/**
 * 
 */
package com.zyh.java8.feature.stream;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

/**
 * three parameter reduce: <br>
 * U reduce(U identity, BiFunction<U, ? super T, U> accumulator, BinaryOperator&ltU&gt combiner);<br>
 * <li>sequential stream, will not call combiner, parallelized stream call comiber</li>
 * <li>if identified is a mutable container, e.g. list, insure it thread safe.</li>
 * 
 * @author zhyhang
 * 
 *
 */
public class ThreeParametersReduct {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// need thread safe for parallel stream
		List<String> list = Collections.synchronizedList(new ArrayList<>());
		System.out.println("input list:" + System.identityHashCode(list));
		System.out.println();
		List<String> reducedList = IntStream.range(0, 1024).mapToObj(String::valueOf).parallel().unordered()
				.reduce(list, (l, s) -> {
					l.add(s);
					return l;
				}, (l1, l2) -> {
					if (l1 != l2) {
						System.out.println("list1 <> list2");
					}
					System.out.println("Thtread: " + Thread.currentThread().getId() + "\tlist1: "
							+ System.identityHashCode(l1) + "\tlist2: " + System.identityHashCode(l2));
					return l1;
				});
		System.out.println();
		System.out.println("reduced list:" + System.identityHashCode(list));
		System.out.println("reduced list size:" + reducedList.size());
	}

}
