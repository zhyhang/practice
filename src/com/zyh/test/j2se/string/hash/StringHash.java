package com.zyh.test.j2se.string.hash;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

/**
 * reference:<br>
 * <a href=
 * "http://stackoverflow.com/questions/1660501/what-is-a-good-64bit-hash-function-in-java-for-textual-strings"
 * >ref1</a><br>
 * <a href="http://www.partow.net/programming/hashfunctions/index.html">ref2</a><br>
 * 
 * @author zhyhang
 */
public class StringHash {

	/**
	 * one million string, test the collision of hash and time cost APHash can
	 * get the good result.
	 * 
	 * @param argv
	 */
	public static void main(String... argv) {

		// random generator string
		int cnt = 10000000;
		int strLength = 22;
		int dupCnt = 0;
		long wholeTimeCost = 0;
		// create the char table
		char[] charTable = new char[64];
		int cti = 0;
		for (int i = 0; i < 10; i++) {
			charTable[cti++] = (char) ('0' + i);
		}
		charTable[cti++] = '_';
		for (int i = 0; i < 26; i++) {
			charTable[cti++] = (char) ('a' + i);
		}
		charTable[cti++] = '-';
		for (int i = 0; i < 26; i++) {
			charTable[cti++] = (char) ('A' + i);
		}
		// using index of char table in every position
		int[] usingIndex = new int[strLength];
		int lastIndex = strLength - 1;
		HashSet<Long> dupHash = new HashSet<Long>((int) (cnt * 1.2));
		char[] csPrefix = "CAESE".toCharArray();
		char[] cs = Arrays.copyOf(csPrefix, strLength + csPrefix.length);
		SingleString ss = new SingleString();
		LongExistVector posVec = new LongExistVector();
		LongExistVector negVec = new LongExistVector();
		for (int i = 0; i < cnt; i++) {
			// range the chars at different positions
			for (int j = lastIndex; j > 0; j--) {
				if (usingIndex[j] >= charTable.length) {
					usingIndex[j] = 0;
					usingIndex[j - 1]++;
				}
			}
			// append chars
			for (int j = 0; j < strLength; j++) {
				cs[csPrefix.length + j] = charTable[usingIndex[j]];
			}
			usingIndex[lastIndex]++;
			ss.setValue(cs);
			// System.out.println(cs);
			// record the time cost
			long ts = System.nanoTime();
			// calculate the string hash
			long hash = GeneralHashFunctionLibrary.CSDNHash(ss);
			// long hash = GeneralHashFunctionLibrary.APHash(ss);
			wholeTimeCost += System.nanoTime() - ts;
			// System.out.println(hash);
			if (hash == -1751414433747795287l || hash == 7353919769594040621l
					|| hash == -4293802507289426314l) {
				System.out.println(ss);
			}

			// if (dupHash.contains(hash)) {
			// dupCnt++;
			// } else {
			// dupHash.add(hash);
			// }

			if (hash < 0) {
				if (negVec.add(hash)) {
					dupCnt++;
				}
			} else {
				if (posVec.add(hash)) {
					dupCnt++;
				}
			}

		}
		System.out.println("Hash collision counter: [" + dupCnt + "].");
		System.out.println("Calculate [" + cnt
				+ "] string hashcode,cost time (ms) : ["
				+ TimeUnit.NANOSECONDS.toMillis(wholeTimeCost) + "].");

	}

	public static class LongExistVector {
		private BitSet modBs0 = new BitSet(Integer.MAX_VALUE);
		private BitSet modBs1 = new BitSet(Integer.MAX_VALUE);
		private BitSet modBs2 = new BitSet();
		private BitSet remainderBs = new BitSet(Integer.MAX_VALUE);

		public boolean add(long value) {
			int remainder = value < 0l ? (int) -(value % (-1l * Integer.MAX_VALUE))
					: (int) (value % Integer.MAX_VALUE);
			long mod = value < 0l ? (value / (-1l * Integer.MAX_VALUE)) : value
					/ Integer.MAX_VALUE;
			BitSet modBs = modBs0;
			int modIndex = (int) mod;
			if (mod >= Integer.MAX_VALUE * 2l) {
				modBs = modBs2;
				modIndex = (int) (mod - Integer.MAX_VALUE * 2l);
			} else if (mod >= Integer.MAX_VALUE) {
				modBs = modBs1;
				modIndex = (int) (mod - Integer.MAX_VALUE);
			}
			boolean exists = remainderBs.get(remainder) && modBs.get(modIndex);
			remainderBs.set(remainder);
			modBs.set(modIndex);
			if (exists) {
				System.out.println(value);
				System.out.println(remainder);
				System.out.println(mod);
				System.out.println(modIndex);
				System.out.println();
			}
			return exists;
		}
	}

	public static class SingleString implements CharSequence {

		private char[] cs;

		@Override
		public int length() {
			return cs.length;
		}

		@Override
		public char charAt(int index) {
			return cs[index];
		}

		@Override
		public CharSequence subSequence(int start, int end) {
			return null;
		}

		public void setValue(char[] cs) {
			this.cs = cs;
		}

		@Override
		public String toString() {
			return new String(cs);
		}

	}

}

// test data
// autohome:
// C83B66E3-CA6C-3536-A342-D97E89C4EE78||2014-02-08 11:43:41.977||hao.360.cn
