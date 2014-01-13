package com.zyh.test.memory.share;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.file.Files;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * 内存共享文件，负责写入文件内容的进程
 * 
 * @author zhyhang
 * 
 */
public class MemoryMapFileCheck {
	public final static long PER_MEM_MAPPING_CAPACITY = 1024 * 1024 * 1024;// 1GB
	public final static long FILE_MIN_CAPACITY = 32l;// 文件最小为32个字节
	public final static int DATA_BLOCK_SIZE = 32;// 数据块大小
	public final static String FILE_NAME = System.getProperty("java.io.tmpdir") + "/share_memory_f_big.data";
	public final static int FILE_CAPACITY = 4;// 4GB
	public final static byte WRITED_BYTE = 'y';// 写入的字符
	public final static int RANDOM_READ_TIMES = 100000;// 随机读取32字节测试次数
	public final static int SLEEP_SECONDS = 300;// 程序暂停时间，以备检查

	private final static Logger logger = Logger.getGlobal();

	static {
		logger.setUseParentHandlers(true);
	}

	public static void create(String file, int size) throws Exception {
		logger.info("File is: " + file);
		File f = new File(file);
		if (!f.exists()) {// create file
			f.createNewFile();
			RandomAccessFile accessFile = new RandomAccessFile(FILE_NAME, "rwd");
			FileChannel channel = accessFile.getChannel();
			for (int i = 0; i < size; i++) {
				MappedByteBuffer mbb = channel.map(MapMode.READ_WRITE, i * (long) PER_MEM_MAPPING_CAPACITY,
						PER_MEM_MAPPING_CAPACITY);
				long j = 0;
				while (j++ < PER_MEM_MAPPING_CAPACITY) {
					mbb.put(WRITED_BYTE);
				}
				mbb.force();
			}
			accessFile.close();
			logger.warning("file creating complete");
		} else {
			logger.warning("file already exists");
		}
	}

	public static boolean read(String file, long testTimes) throws Exception {
		String filename = file;
		File f = checkReadFile(filename);
		if (f == null) {
			return false;
		}
		RandomAccessFile accessFile = new RandomAccessFile(f, "r");
		long fileSize = accessFile.length();
		int segNum = (int) (fileSize / PER_MEM_MAPPING_CAPACITY);
		if (segNum * PER_MEM_MAPPING_CAPACITY < fileSize) {
			segNum++;
		}
		MappedByteBuffer[] memMappingSegs = new MappedByteBuffer[segNum];
		FileChannel channel = accessFile.getChannel();
		for (int i = 0; i < segNum; i++) {
			MappedByteBuffer mbb = channel.map(MapMode.READ_ONLY, i * PER_MEM_MAPPING_CAPACITY,
					PER_MEM_MAPPING_CAPACITY);
			long beginMs = System.currentTimeMillis();
			mbb.load();// load in physical memory
			memMappingSegs[i] = mbb;
			logger.info("Taking " + (System.currentTimeMillis() - beginMs)
					+ " ms loading to memory. Content in memory? " + mbb.isLoaded());
		}
		Random rd = new Random();
		long maxLatency = 0;
		long ltEq5us = 0;
		long ltEq10us = 0;
		long ltEq20us = 0;
		long ltEq40us = 0;
		long ltEq80us = 0;
		long gt80us = 0;
		byte[] buffer = new byte[DATA_BLOCK_SIZE];
		long beginTs = System.nanoTime();
		long middleTs = beginTs;
		MappedByteBuffer mbb = null;
		for (int i = 0; i < testTimes; i++) {
			long randomPos = Math.abs(rd.nextLong()) % fileSize;
			int memMappingIndex = (int) (randomPos / PER_MEM_MAPPING_CAPACITY);
			mbb = memMappingSegs[memMappingIndex];
			int pos = (int) (randomPos % PER_MEM_MAPPING_CAPACITY);
			for (int j = 0; j < buffer.length; j++) {
				buffer[j] = mbb.get(pos + j);
			}
			long perLatency = System.nanoTime() - middleTs;
			middleTs = System.nanoTime();
			if (perLatency >= maxLatency) {
				maxLatency = perLatency;
			}
			if (perLatency <= 5000) {
				ltEq5us++;
			} else if (perLatency <= 10000) {
				ltEq10us++;
			} else if (perLatency <= 20000) {
				ltEq20us++;
			} else if (perLatency <= 40000) {
				ltEq40us++;
			} else if (perLatency <= 80000) {
				ltEq80us++;
			} else {
				gt80us++;
			}
		}
		long latencyNs = (System.nanoTime() - beginTs);
		System.out.println("latency time / per-read: " + TimeUnit.NANOSECONDS.toMicros(latencyNs) / RANDOM_READ_TIMES
				+ " us (mirco-second).");
		System.out.println("max latency time: " + TimeUnit.NANOSECONDS.toMicros(maxLatency) + " us (mirco-second).");
		System.out.println("<=5us\t<=10us\t<=20us\t<=40us\t<=80us\t...");
		System.out.println(ltEq5us + "\t" + ltEq10us + "\t" + ltEq20us + "\t" + ltEq40us + "\t" + ltEq80us + "\t"
				+ gt80us);
		System.out.println("get char: " + (char) mbb.get(1024 * 1024));
		channel.close();
		accessFile.close();
		TimeUnit.SECONDS.sleep(SLEEP_SECONDS);
		return true;
	}

	private static File checkReadFile(String filename) throws IOException {
		logger.info("File is: " + filename);
		File f = new File(filename);
		if (!f.exists()) {
			String msg = filename + " not found!";
			logger.severe(msg);
			return null;
		}
		long filesize = Files.size(f.toPath());
		if (filesize < FILE_MIN_CAPACITY) {
			logger.severe("too small data in file " + filename);
			return null;
		}
		return f;
	}

	public static void main(String[] argv) throws Exception {
		if (argv.length > 0) {
			String cmd = argv[0];
			String file = null;
			if (argv.length > 1) {
				file = argv[1];
			}
			file = file == null ? FILE_NAME : file;
			int size = FILE_CAPACITY;
			if (argv.length > 2) {
				size = Integer.valueOf(argv[2]);
			}
			long testTimes = RANDOM_READ_TIMES;
			if(argv.length>3){
				testTimes=Long.valueOf(argv[3]);
			}
			if ("read".equalsIgnoreCase(cmd)) {
				read(file, testTimes);
				return;
			} else if ("create".equalsIgnoreCase(cmd)) {
				create(file, size);
				return;
			} else if ("write".equalsIgnoreCase(cmd)) {
				return;
			}
		}
		logger.severe("specify the args: <create/read/write> file [filesize(gb)] [test read times]");
	}

}