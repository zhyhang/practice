/**
 * 
 */
package com.zyh.test.perform.tips;

import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.FastDateFormat;

/**
 * @author zhyhang
 * 
 */
public class ApacheDateFormat {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// this test is nothing, because DateFormatUtils inner implements is FastDateFormat.
		// function validity test
		functionTest();
		// performance test
		performaceTest();
	}

	private static void functionTest() {
		long errCount = 0;
		long ts = System.currentTimeMillis();
		ts = ts - TimeUnit.DAYS.toMillis(30);
		String pattern = "yyyyMMddHHmmssSSS";
		FastDateFormat a3formatter = FastDateFormat.getInstance(pattern);
		int testCount = 1000000;
		System.out.println("apache-date-format\t\t\t\tapache3-date-format");
		for (int i = 0; i < testCount; i++) {
			ts += 10000 * Math.random() + 1;
			String astr = DateFormatUtils.format(ts, pattern);
			String a3str = a3formatter.format(ts);
			if (!a3str.equals(astr)) {
				errCount++;
				System.out.print(astr);
				System.out.print("\t\t\t\t");
				System.out.println(a3str);
			}
		}
		System.out.format("Total times of difference [%d] .", errCount);
	}

	private static void performaceTest() {
		long formatTs = System.currentTimeMillis();
		formatTs = formatTs - TimeUnit.DAYS.toMillis(30);
		String pattern = "yyyyMMddHHmmssSSS";
		FastDateFormat a3formatter = FastDateFormat.getInstance(pattern);
		int testCount = 10000000;
		long[] allTs = new long[testCount];
		for (int i = 0; i < testCount; i++) {
			formatTs += 10000 * Math.random() + 1;
			allTs[i] = formatTs;
		}
		long ts = System.nanoTime();
		for (int i = 0; i < testCount; i++) {
			a3formatter.format(allTs[i]);
		}
		long fts = System.nanoTime();
		for (int i = 0; i < testCount; i++) {
			DateFormatUtils.format(allTs[i], pattern);
		}
		long ats = System.nanoTime();
		System.out.println("apache fast date format cost(ms):" + TimeUnit.NANOSECONDS.toMillis(fts - ts));
		System.out.println("apache date format cost(ms):" + TimeUnit.NANOSECONDS.toMillis(ats - fts));
	}
}
