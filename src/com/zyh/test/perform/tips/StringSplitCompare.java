package com.zyh.test.perform.tips;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

public class StringSplitCompare {

	public static void main(String[] args) {
		String[] randStrings = new String[3];
		// random generate three strings
		randStrings[0] = RandomStringUtils.random(16, '0', '1');
		randStrings[1] = RandomStringUtils.random(512, '0', '1');
		randStrings[2] = RandomStringUtils.random(1024, '0', '1');
		// hot the cache
		for (String s : randStrings) {
			s.split("0");
			StringUtils.split(s, '0');
		}
		printStrings("boo:and:foo".split("o"));
		printStrings(StringUtils.splitPreserveAllTokens("boo:and:foo", "o"));
		// begin compare
		int iterateTimes = 100000;
		long ts = System.currentTimeMillis();
		for (int i = 0; i < iterateTimes; i++) {
			for (String s : randStrings) {
				s.split("0");
			}
		}
		long jdkCost = System.currentTimeMillis() - ts;
		ts = System.currentTimeMillis();
		for (int i = 0; i < iterateTimes; i++) {
			for (String s : randStrings) {
				StringUtils.split(s, '0');
			}
		}
		long apacheCost = System.currentTimeMillis() - ts;
		System.out.println("Jdk split vs Apache split:");
		System.out.format("Iterate Count: %,d\n", iterateTimes);
		System.out.println("Time Cost (ms) :");
		System.out.println("-----------------------------");
		System.out.println("Jdk split\tApache split");
		System.out.println("-----------------------------");
		System.out.format("%-,9d\t%-,9d\n", jdkCost, apacheCost);
		System.out.println("-----------------------------");
	}

	private static void printStrings(String[] ss) {
		System.out.print("[");
		for (int i = 0; i < ss.length; i++) {
			String s = ss[i];
			if (i < ss.length - 1) {
				System.out.print("\"" + s + "\",");
			} else {
				System.out.print("\"" + s + "\"");
			}
		}
		System.out.println("]");

	}

}
