/**
 * 
 */
package com.zyh.test.j2se.performace.serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import net.jpountz.lz4.LZ4Factory;

/**
 * @author zhyhang
 *
 */
public class LongIntSerializer {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// prepare data
		int length = 100000;
		long[][] rows1 = new long[length][2];
		int[][] rows2 = new int[length][3];
		long[][] desRows1 = new long[length][rows1[0].length];
		int[][] desRows2 = new int[length][rows2[0].length];
		// prepare data
		prepare(length, rows1, rows2);
		// self
		byte[] sBytes = toBytes(rows1, rows2);
		System.out.format("serialized bytes length %d.\n", sBytes.length);
		parseBytes(sBytes, desRows1, desRows2);
		// check
		check(rows1, rows2, desRows1, desRows2);
		// jdk
		Object[] aggreData = new Object[] { rows1, rows2 };
		byte[] sBytesJdk = toBytesJdk(aggreData);
		System.out.format("Jdk serialized bytes length %d.\n", sBytesJdk.length);
		parseBytesJdk(sBytesJdk);
		// lz4
		byte[] sBytesLz4 = toBytesLz4(rows1, rows2);
		System.out.format("Lz4 serialized bytes length %d.\n", sBytesLz4.length);
		parseBytesLz4(sBytesLz4, desRows1, desRows2, sBytes.length);
		// check
		check(rows1, rows2, desRows1, desRows2);
		// performance test
		long[] timecost = new long[6];
		long[] serialsize = new long[3];
		int testCount = 1000;
		for (int i = 0; i < testCount; i++) {
			prepare(length, rows1, rows2);
			// self serializer
			long tsb = System.nanoTime();
			byte[] sbs = toBytes(rows1, rows2);
			int bytesLength = sbs.length;
			timecost[0] += System.nanoTime() - tsb;
			serialsize[0] += sbs.length;
			tsb = System.nanoTime();
			parseBytes(sbs, desRows1, desRows2);
			timecost[1] += System.nanoTime() - tsb;
			// jdk serializer
			tsb = System.nanoTime();
			sbs = toBytesJdk(new Object[] { rows1, rows2 });
			timecost[2] += System.nanoTime() - tsb;
			serialsize[1] += sbs.length;
			tsb = System.nanoTime();
			parseBytesJdk(sbs);
			timecost[3] += System.nanoTime() - tsb;
			// lz4 serializer
			tsb = System.nanoTime();
			sbs = toBytesLz4(rows1, rows2);
			timecost[4] += System.nanoTime() - tsb;
			serialsize[2] += sbs.length;
			tsb = System.nanoTime();
			parseBytesLz4(sbs, desRows1, desRows2, bytesLength);
			timecost[5] += System.nanoTime() - tsb;
		}
		System.out.format(
				"test-count[%d],row-length[%d],self/jdk/lz4[serial-time,deserial-time,serial-size][%d/%d/%d,%d/%d/%d,%d/%d/%d]\n",
				testCount, length, milli(timecost[0]), milli(timecost[2]), milli(timecost[4]), milli(timecost[1]),
				milli(timecost[3]), milli(timecost[5]), serialsize[0], serialsize[1], serialsize[2]);
	}

	private static long milli(long nano) {
		return TimeUnit.NANOSECONDS.toMillis(nano);
	}

	private static void prepare(int length, long[][] rows1, int[][] rows2) {
		for (int i = 0; i < length; i++) {
			rows1[i] = new long[] { ThreadLocalRandom.current().nextLong(90000000), System.currentTimeMillis() };
			rows2[i] = new int[] { ThreadLocalRandom.current().nextInt(66666667),
					ThreadLocalRandom.current().nextInt(6667), ThreadLocalRandom.current().nextInt(667) };
		}
	}

	private static void check(long[][] rows1, int[][] rows2, long[][] desRows1, int[][] desRows2) {
		for (int i = 0; i < rows1.length; i++) {
			for (int j = 0; j < rows1[i].length; j++) {
				if (rows1[i][j] != desRows1[i][j]) {
					System.out.format("rows1-diff[%d,%d]\n", rows1[i][j], desRows1[i][j]);
				}
			}
			for (int j = 0; j < rows1[i].length; j++) {
				if (rows2[i][j] != desRows2[i][j]) {
					System.out.format("rows1-diff[%d,%d]\n", rows2[i][j], desRows2[i][j]);
				}
			}
		}
	}

	private static byte[] toBytes(long[][] rows1, int[][] rows2) {
		int length = rows1.length * (rows1[0].length * Long.BYTES) + rows2.length * (rows2[0].length * Integer.BYTES);
		ByteBuffer bb = ByteBuffer.allocate(length);
		for (int i = 0; i < rows1.length; i++) {
			for (int j = 0; j < rows1[i].length; j++) {
				bb.putLong(rows1[i][j]);
			}
			for (int j = 0; j < rows2[i].length; j++) {
				bb.putInt(rows2[i][j]);
			}
		}
		return bb.array();
	}

	private static void parseBytes(byte[] sBytes, long[][] rows1, int[][] rows2) {
		ByteBuffer bb = ByteBuffer.wrap(sBytes);
		for (int i = 0; i < rows1.length; i++) {
			for (int j = 0; j < rows1[i].length; j++) {
				rows1[i][j] = bb.getLong();
			}
			for (int j = 0; j < rows2[i].length; j++) {
				rows2[i][j] = bb.getInt();
			}
		}
	}

	private static byte[] toBytesJdk(Object[] data) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream(256);
		try {
			ObjectOutputStream oos = new ObjectOutputStream(bos);
			oos.writeObject(data);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return bos.toByteArray();
	}

	private static Object[] parseBytesJdk(byte[] sBytes) {
		ByteArrayInputStream bis = null;
		ObjectInputStream ois = null;
		try {
			bis = new ByteArrayInputStream(sBytes);
			ois = new ObjectInputStream(bis);
			return (Object[]) ois.readObject();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private static byte[] toBytesLz4(long[][] rows1, int[][] rows2) {
		return LZ4Factory.fastestInstance().fastCompressor().compress(toBytes(rows1, rows2));
	}

	private static void parseBytesLz4(byte[] sBytes, long[][] rows1, int[][] rows2, int decompSize) {
		parseBytes(LZ4Factory.fastestInstance().fastDecompressor().decompress(sBytes, decompSize), rows1, rows2);
	}

}
