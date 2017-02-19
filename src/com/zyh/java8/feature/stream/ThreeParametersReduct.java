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
		List<String> reducedList = IntStream.range(0, 128).mapToObj(String::valueOf).parallel().unordered().reduce(list,
				(l, s) -> {
					l.add(s);
					return l;
				}, (l1, l2) -> {
					if (l1 != l2) {
						System.out.println("list1 <> list2");
					}
					System.out.println("Thread: " + Thread.currentThread().getId() + "\tlist1: "
							+ System.identityHashCode(l1) + "\tlist2: " + System.identityHashCode(l2));
					return l1;
				});
		System.out.println();
		System.out.println("reduced list:" + System.identityHashCode(list));
		System.out.println("reduced list elements:" + list);
		System.out.println("reduced list size:" + reducedList.size());

		System.out.println();
		List<String> list2 = IntStream.range(0, 128).mapToObj(String::valueOf).parallel().unordered().collect(() -> {
			List<String> nlist = new ArrayList<>();
			System.out.println("Thread-supplier: " + Thread.currentThread().getId() + "\tnew list: "
					+ System.identityHashCode(nlist));
			return nlist;
		}, (l, s) -> {
			l.add(s);
			System.out.println("Thread-accumulator: " + Thread.currentThread().getId() + "\tlist: "
					+ System.identityHashCode(l));
		}, (l1, l2) -> {
			l1.addAll(l2);
			System.out.println("Thread-combiner: " + Thread.currentThread().getId() + "\tlist1: "
					+ System.identityHashCode(l1) + "\tlist2: " + System.identityHashCode(l2));
		});
		System.out.println();
		System.out.println("reduced list2:" + System.identityHashCode(list2));
		System.out.println("reduced list2 elements:" + list2);
		System.out.println("reduced list2 size:" + list2.size());

	}

}
