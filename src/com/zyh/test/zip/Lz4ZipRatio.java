/**
 * 
 */
package com.zyh.test.zip;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;
import java.util.zip.GZIPOutputStream;

import net.jpountz.lz4.LZ4Compressor;
import net.jpountz.lz4.LZ4Factory;

/**
 * @author zhyhang
 *
 */
public class Lz4ZipRatio {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		long[] ldata=new long[400000];
		Arrays.parallelSetAll(ldata, i->ThreadLocalRandom.current().nextLong(6667));
		ByteArrayOutputStream bo = new ByteArrayOutputStream();
		ObjectOutputStream oo = new ObjectOutputStream(bo);
		oo.writeObject(ldata);
		oo.flush();
		byte[] bdata=bo.toByteArray();
		LZ4Compressor fc = LZ4Factory.fastestInstance().fastCompressor();
		fc.maxCompressedLength(bdata.length);
		byte[] bdatacompr = fc.compress(bdata);
		System.out.format("before/after-compress-size[%d/%d].\n",bdata.length,bdatacompr.length);
	}

}
