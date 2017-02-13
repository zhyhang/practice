/**
 * 
 */
package com.zyh.java8.feature.forkjoin;

import java.util.concurrent.ForkJoinPool;

/**
 * @author zhyhang
 *
 */
public class CommonPoolTry {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", "20");
		// or start parameter -Djava.util.concurrent.ForkJoinPool.common.parallelism=n
		// default 1 less than #cores
		System.out.format("ForkJoinPool.getCommonPoolParallelism[%d]\n", ForkJoinPool.getCommonPoolParallelism());
	}

}
