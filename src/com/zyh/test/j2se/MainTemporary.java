/**
 * 
 */
package com.zyh.test.j2se;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.DoubleUnaryOperator;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.alibaba.fastjson.JSON;

import jodd.lagarto.dom.Document;
import jodd.lagarto.dom.LagartoDOMBuilder;

/**
 * 
 * using for temporary test,try,output...
 * 
 * @author zhyhang
 *
 */
public class MainTemporary {

	private static int stackDeep = 0;

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		// long ts = System.currentTimeMillis();
		// System.out.format("%tY%tm%td%tH%tM%tS\n", ts, ts, ts, ts, ts, ts);
		// System.out.format("%d,%d\n", 0b00001000,
		// Long.numberOfTrailingZeros(0b00001000));
		// mapIterateRemove();
		// printMap();
		// extractFilePage();
		// extractHttpPage();
		// tryFinally();
		// tryMainValuesOrder();
		// tryLocalNameIp();
		// fileTimeCheck();
		// intToHexBytes();
		// sortChinese();
		// intToHexBytes();
		// streamIssue();
		// stackDeep();
		// splitStringReserve();
		// uuidJava();
		// completableFutureLearn();
		// ipAddressCodec();
		// checkLambdaInstance();
		// checkLambdaInstance();
		lambdaSerialize();

	}

	private static void extractFilePage() throws Exception {
		String pyadPage = new String(
				Files.readAllBytes(Paths.get(MainTemporary.class.getResource("/iframe_dynamic_create.html").toURI())),
				StandardCharsets.UTF_8);
		System.out.printf("original page content:\n%s\nencoded:\n%s\n", pyadPage,
				Base64.getEncoder().encodeToString(pyadPage.getBytes(StandardCharsets.UTF_8)));

	}

	private static void extractHttpPage() throws IOException, ClientProtocolException {
	}

	private static void printMap() {
		List<Long[]> reqObj = new ArrayList<>();
		// linux shell date -d'2015-11-18' +%s get secornd since 1970
		// add "000" to milli
		reqObj.add(new Long[] { 1447776000000L });
		reqObj.add(new Long[] { 16922L });
		reqObj.add(new Long[] { 15966L });
		reqObj.add(new Long[] { 20206L });
		reqObj.add(new Long[] { 63332L });
		System.out.println(JSON.toJSONString(reqObj));
	}

	private static void mapIterateRemove() {
		Map map = new HashMap<Integer, Integer>();
		map.put(1, 1);
		map.put(2, 2);
		map.put(3, 3);
		Iterator<Map.Entry<Integer, Integer>> itr = map.entrySet().iterator();
		while (itr.hasNext()) {
			Entry<Integer, Integer> entry = itr.next();
			if (entry.getKey().equals(Integer.valueOf(2))) {
				itr.remove();
			}
		}
		System.out.println(JSON.toJSONString(map));
	}

	private static void tryFinally() {
		for (int i = 0; i < 10; i++) {
			try (PrintWriter pw = new PrintWriter("/data/temp/pwt.1.txt")) {
				if (i % 2 == 0) {
					continue;
				}
			} catch (Exception e) {

			} finally {
				System.out.println("enter finally.");
			}

		}
	}

	private static void tryMainValuesOrder() {
		Map<String, String> map = new LinkedHashMap<>();
		map.put("img1", "img1");
		map.put("img2", "img2");
		System.out.println("map.values:");
		map.values().forEach(s -> {
			System.out.format("%s\t", s);
		});
	}

	private static void tryLocalNameIp() throws IOException {
		System.out.println();
		System.out.println(InetAddress.getLocalHost().getHostName());
		System.out.println("Your Host addr: " + InetAddress.getLocalHost().getHostAddress()); // often
																								// returns
																								// "127.0.0.1"
		try (ServerSocket ss1 = new ServerSocket(0)) {
			System.out.println(ss1.getLocalSocketAddress().toString());
		}

		Enumeration<NetworkInterface> n = NetworkInterface.getNetworkInterfaces();
		for (; n.hasMoreElements();) {
			NetworkInterface e = n.nextElement();

			Enumeration<InetAddress> a = e.getInetAddresses();
			for (; a.hasMoreElements();) {
				InetAddress addr = a.nextElement();
				System.out.format("ip:[%s], isloopback[%s].\n", addr.getHostAddress(), e.isLoopback());
			}
		}
	}

	private static void fileTimeCheck() throws Exception {
		// on linux accurate to second
		// on windows accurate to nanosecond
		String fileName = "/data/temp/ExampleZipReplicatedCache.dat.0";
		FileTime fileTime = Files.getLastModifiedTime(Paths.get(fileName));
		System.out.format("Files: file time [%s], millis[%d], nanos[%d]\n", fileTime.toString(), fileTime.toMillis(),
				fileTime.to(TimeUnit.NANOSECONDS));
		File file = new File(fileName);
		System.out.format("File: file time millis[%d]\n", file.lastModified());
	}

	public static void intToHexBytes() {
		int value = 1024;
		ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
		buffer.putInt(value);
		buffer.rewind();
		System.out.println("int bytes:");
		byte[] ibs = new byte[Integer.BYTES];
		for (int i = 0; i < Integer.BYTES; i++) {
			System.out.println(buffer.get());
		}
		for (int i = 0; i < Integer.BYTES; i++) {
			byte b = ((byte) (value >> (32 - (i + 1) << 3)));
			ibs[i] = b;
			byte hb = (byte) (0x0f & (b >> 4));
			if (hb <= 9 && hb >= 0) {
				hb += 0x30;
			} else {
				hb += 0x37;
			}
			byte lb = (byte) (0x0f & b);
			if (lb <= 9 && lb >= 0) {
				lb += 0x30;
			} else {
				lb += 0x37;
			}
			System.out.println(new String(new byte[] { hb, lb }, StandardCharsets.US_ASCII));
		}
		System.out.println(Integer.toHexString(value));
		System.out.println(value);
		System.out.println(new String(ibs));
		System.out.println("end");
	}

	public static void streamIssue() {
		System.out.println();
		longs().limit(5).forEach(System.out::println);
	}

	public static Stream<Long> longs() {
		return Stream.iterate(1L, i -> 1L + longs().skip(i - 1L).findFirst().get());
	}

	public static void stackDeep() {
		System.err.println(++stackDeep);
		stackDeep();
	}

	public static void sortChinese() {
		String sortingStr = "杩欐槸涓�涓猅his is a涓枃绠�鍗曞瓧绗︿覆锛屼笉鏄あSimple Chinese string.";
		StringBuilder sortedSb = new StringBuilder(sortingStr.length());
		sortingStr.codePoints().mapToObj(Character::toChars)
				.sorted((chs1, chs2) -> Collator.getInstance(Locale.CHINA).compare(new String(chs1), new String(chs2)))
				.reduce(sortedSb, (sb, chs) -> sb.append(chs), (sb1, sb2) -> sb2);
		System.out.println("\n" + sortingStr);
		System.out.println("\n" + sortedSb);
	}

	public static void splitStringReserve() {
		String s = "tid::63133";
		String[] splited = StringUtils.splitByWholeSeparatorPreserveAllTokens(s, ":");
		System.out.println(splited.length + "\t" + Arrays.deepToString(splited));
	}

	public static void uuidJava() {
		System.out.println(UUID.randomUUID());
	}

	public static void completableFutureLearn() {
		CompletableFuture<String> cf = CompletableFuture.completedFuture("I'm done!");
		System.out.format("isDone[%b], join[%s]\n", cf.isDone(), cf.join());
	}

	public static void ipAddressCodec() {
		InetSocketAddress remote = new InetSocketAddress("192.168.152.220", 1024);
		System.out.println(remote.getPort());
		System.out.println(remote.getAddress().getHostAddress());
		for (byte b : remote.getAddress().getAddress()) {
			System.out.println(b);
		}
		byte[] ipbs = remote.getAddress().getAddress();
		long ipport = Byte.toUnsignedLong(ipbs[0]) << 56 | Byte.toUnsignedLong(ipbs[1]) << 48
				| Byte.toUnsignedLong(ipbs[2]) << 40 | Byte.toUnsignedLong(ipbs[3]) << 32
				| Integer.toUnsignedLong(remote.getPort());
		System.out.println((ipport >>> 56) + "." + (ipport >>> 48 & 0xff) + "." + (ipport >>> 40 & 0xff) + "."
				+ (ipport >>> 32 & 0xff) + ":" + (ipport & 0xffff));
	}

	public static void checkLambdaInstance() {
		Map<Integer, Integer> map = new HashMap<>();
		map.put(1, 1);
		map.put(2, 2);
		final StringBuilder sb = new StringBuilder(8);
		Stream<Entry<Integer, Integer>> stream = map.entrySet().stream();
		// below, every lambda only one instance, although main calling two times
		stream.reduce(sb, MainTemporary::lambda1, (sb1, sb2) -> sb1);
		// below, second lambda will new one instance every call
		// stream.reduce(sb, MainTemporary::lambda1, (sb1, sb2) -> sb);
		System.out.println(sb);
	}

	private static StringBuilder lambda1(StringBuilder sb, Entry<Integer, Integer> e) {
		sb.append(e.getKey().intValue()).append(',');
		return sb;
	}

	private static void lambdaSerialize() {
		int coefficient = 3;
		DoubleUnaryOperator duo = (DoubleUnaryOperator & Serializable) d -> {
			return Math.sqrt(d) * coefficient;
		};
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(duo);
			byte[] seData = baos.toByteArray();
			ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(seData));
			DoubleUnaryOperator duoDes = (DoubleUnaryOperator) ois.readObject();
			System.out.println(duoDes.applyAsDouble(4.0));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
