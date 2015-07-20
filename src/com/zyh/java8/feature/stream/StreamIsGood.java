/**
 * 
 */
package com.zyh.java8.feature.stream;

import java.util.Arrays;
import java.util.stream.IntStream;

/**
 * @author zhyhang
 *
 */
public class StreamIsGood {

	public static void main(String[] args) {
		Integer[] Is= IntStream.range(0, 100).boxed().toArray(i->new Integer[i]);
		System.out.println(Arrays.deepToString(Is));
	}

}
