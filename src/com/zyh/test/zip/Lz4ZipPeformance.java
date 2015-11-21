/**
 * 
 */
package com.zyh.test.zip;

import java.util.concurrent.TimeUnit;

import net.jpountz.lz4.LZ4Compressor;
import net.jpountz.lz4.LZ4Factory;
import net.jpountz.lz4.LZ4FastDecompressor;
import net.jpountz.lz4.LZ4SafeDecompressor;

/**
 * @author zhyhang
 *
 */
public class Lz4ZipPeformance {
	
	public static void main(String[] argv) throws Exception{
		LZ4Factory factory = LZ4Factory.fastestInstance();
		
		byte[] data = "953295:1:0.39:3900000;19:0.39:3900000,17:0.39:3900000,18:0.39:3900000,15:0.39:3900000,16:0.39:3900000,13:0.39:3900000,14:0.39:3900000,11:0.39:3900000,12:0.39:3900000,21:0.39:3900000,20:0.39:3900000,22:0.39:3900000,23:0.39:3900000,24:0.39:3900000,25:0.39:3900000,26:0.39:3900000,27:0.39:3900000,28:0.39:3900000,29:0.39:3900000,3:0.39:3900000,2:0.39:3900000,10:0.39:3900000,1:0.39:3900000,0:0.39:3900000,30:0.39:3900000,7:0.39:3900000,6:0.39:3900000,5:0.39:3900000,4:0.39:3900000,9:0.39:3900000,8:0.39:3900000953295:1:0.39:3900000;19:0.39:3900000,17:0.39:3900000,18:0.39:3900000,".getBytes("UTF-8");
		final int decompressedLength = data.length;

		// compress data
		LZ4Compressor compressor = factory.fastCompressor();
		int maxCompressedLength = compressor.maxCompressedLength(decompressedLength);
		byte[] compressed = new byte[maxCompressedLength];
		int compressedLength = compressor.compress(data, 0, decompressedLength, compressed, 0, maxCompressedLength);
		
		System.out.println(decompressedLength+","+compressedLength);

		// decompress data
		// - method 1: when the decompressed length is known
		LZ4FastDecompressor decompressor = factory.fastDecompressor();
		byte[] restored = new byte[decompressedLength];
		int compressedLength2 = decompressor.decompress(compressed, 0, restored, 0, decompressedLength);
		// compressedLength == compressedLength2

		// - method 2: when the compressed length is known (a little slower)
		// the destination buffer needs to be over-sized
		LZ4SafeDecompressor decompressor2 = factory.safeDecompressor();
		int decompressedLength2 = decompressor2.decompress(compressed, 0, compressedLength, restored, 0);
		// decompressedLength == decompressedLength2
		long ts = System.currentTimeMillis();
		System.out.format("[%d]ms,[%d]s,[%d]s/year,[%d]s/decade.\n",ts,TimeUnit.MILLISECONDS.toSeconds(ts),TimeUnit.DAYS.toSeconds(365),
				TimeUnit.DAYS.toSeconds(365 * 10));
		byte[] sbs = "yHqR5LF3BtP2cjCApqpEje9OG2fb4Bpkg4jQjWqddm2O9oDd7r1fuD3jyY6gcIdOswu2RxTHSPNqU5UWeF12GzRiIbRNbjAsve7fFSFBg7on3NckdEDtGw1NdtTp15XT3wyNgOxTXcHOMu65XbMZvuEDASKX0XIytfQsZbjwlAh6Ufjcpzv1ruS7hU3RHDr8rppCOhz7ycbzhYvNkVMFOPw2WnNKV2tjn9IjP81MIhyyue3WeCQIBYCkH7o9VIjMU6BAKQbce7gDbnVkCwBA7yjdvhFGYl3ynwkWaCICyvuIYykAdQhTUucWfP3eV4HTApPPHc3NLsSLxyxY6W833QyMTQvULhywqslb1GgF7sclbSZzhGBTXGvAl6rzvUKlHBlX4Z2Hi6Ji1uekuMTKbJ0ICSV9DY1QVwQmFnbxudp2Qx7uCqmpuVSfSAUL9yuTQzNs8GLXuNcknmztDYqKw0iOEDw2IaChccQAcGPvQGzJDbCOEeVFfCEJIxKe24f5TBJQBLyWqTufIkFv0GxEyCjZz4JVQ5i74U6lWF0MiRfDMdVA7t2aO5bLmirVI8xwkwxal6fYsIi50La4KrHQ8ajMDpHw3c3k6UbaZpT96FzCjIao7x6j5qcyc8Pm4gM".getBytes();
		
		System.out.println(sbs.length);
	}

}
