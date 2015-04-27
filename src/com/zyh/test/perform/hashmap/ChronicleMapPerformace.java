/**
 * 
 */
package com.zyh.test.perform.hashmap;

import java.io.File;
import java.io.IOException;

import net.openhft.chronicle.map.ChronicleMap;
import net.openhft.chronicle.map.ChronicleMapBuilder;
import net.openhft.lang.model.constraints.MaxSize;

import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ChronicleMap performace check
 * 
 * v2.1.4 possible memory leak, when using String type in Value class. see StringIntener 
 *
 * 
 * @author zhyhang
 *
 */
public class ChronicleMapPerformace {

	private static ChronicleMap<String, ExpiringVo48> cm = null;

	private static ChronicleMap<String, ExpiringVo128> cm_128 = null;

	private static ChronicleMap<String, ExpiringVo640> cm_640 = null;

	private static ChronicleMap<String, ExpiringVo1> cm_1 = null;

	private static Logger logger = LoggerFactory.getLogger(ChronicleMapPerformace.class);

	public static void main(String... argv) throws Exception {
		long tsb = System.currentTimeMillis();
		cm = initMap("cm_700K_48Len.cache", ExpiringVo48.class, ExpiringVo48.MAX_SIZE);
		cm_128 = initMap("cm_200K_128Len.cache", ExpiringVo128.class, ExpiringVo128.MAX_SIZE);
		cm_640 = initMap("cm_300K_640Len.cache", ExpiringVo640.class, ExpiringVo640.MAX_SIZE);
		cm_1 = initMap("cm_300K_1Len.cache", ExpiringVo1.class, ExpiringVo1.MAX_SIZE);
		System.out.format("initMap cost: %dms.\n", System.currentTimeMillis() - tsb);
		tsb = System.currentTimeMillis();
		fillMap(cm, ExpiringVo48.INFO_SIZE - 1, ExpiringVo48.MAX_SIZE);
		fillMap(cm_128, ExpiringVo128.INFO_SIZE - 1, ExpiringVo128.MAX_SIZE);
		fillMap(cm_640, ExpiringVo640.INFO_SIZE - 2, ExpiringVo640.MAX_SIZE);
		fillMap(cm_1, ExpiringVo1.INFO_SIZE - 1, ExpiringVo1.MAX_SIZE);
		System.out.format("fillMap cost: %dms.\n", System.currentTimeMillis() - tsb);
		System.out.format("Map48 size: %d,Map128 size:%d,Map640 size:%d,Map1 size:%d.\n", cm.size(), cm_128.size(),
				cm_640.size(), cm_1.size());
		System.out.format("access counter, Map48:%d,Map128:%d,Map640:%d,Map1:%d.\n", cm.entrySet().iterator().next()
				.getValue().getAccessCounter(), cm_128.entrySet().iterator().next().getValue().getAccessCounter(),
				cm_640.entrySet().iterator().next().getValue().getAccessCounter(), cm_1.entrySet().iterator().next()
						.getValue().getAccessCounter());
		for (int i = 0; i < 5; i++) {
			tsb = System.currentTimeMillis();
			readMap(cm);
			readMap(cm_128);
			readMap(cm_640);
			readMap(cm_1);
			System.out.format("%d readMap cost: %dms.\n", i + 1, System.currentTimeMillis() - tsb);
			Thread.sleep(10000);
		}
		Thread.sleep(10000);
	}

	private static <T extends ExpiringVo> ChronicleMap<String, T> initMap(String fileName, Class<T> valueClazz,
			int entrySize) {
		final String basePath = System.getProperty("java.io.tmpdir") + File.separator + "test";
		File cacheFile = new File(basePath + File.separator + fileName);
		try {
			return ChronicleMapBuilder.of(String.class, valueClazz).entries(entrySize).createPersistedTo(cacheFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static <T extends ExpiringVo> void fillMap(ChronicleMap<String, T> rcm, int infoSize, int maxSize) {
		if (rcm != null && rcm.size() > 0) {
			return; // ignore
		}
		for (int i = 0; i < maxSize; i++) {
			String key = "u_**********_**********_**********_".concat(String.valueOf(i));
			T vo = rcm.newValueInstance();
			String info = RandomStringUtils.randomAlphanumeric(infoSize);
			vo.setInfo(info);
			vo.setTime((int) (System.currentTimeMillis() % Integer.MAX_VALUE));
			try {
				rcm.put(key, vo);
			} catch (Exception e) {
				logger.info("Error in put entry at " + vo.getClass().getSimpleName(), e);
				break; // over the size
			}
		}
	}

	private static <T extends ExpiringVo> void readMap(ChronicleMap<String, T> rcm) {
		// System.out.println(rcm.get("u_**********_**********_**********_8888"));
		for (String key : rcm.keySet()) {
			// T usingVo = rcm.newValueInstance();
			ExpiringVo vo = rcm.get(key);// rcm.getUsing(entry.getKey(),
											// usingVo);
			int counter = vo.getAccessCounter();
			counter = counter < Integer.MAX_VALUE ? counter + 1 : counter;
			vo.setAccessCounter(counter);
			vo.setTime(vo.getTime() + 1);
			vo.getInfo();
		}
		// for (String key:rcm.keySet()) {
		// rcm.get("no exists");
		// ExpiringVo vo = rcm.get(key);//cm.getUsing(key, usingVo);
		// int counter=vo.getAccessCounter();
		// counter = counter<Integer.MAX_VALUE?counter+1:counter;
		// vo.setAccessCounter(counter);
		// vo.getInfo();
		// }
	}

	public static interface ExpiringVo {
		int getTime();

		void setTime(int ts);

		int getAccessCounter();

		void setAccessCounter(int counter);

		String getInfo();

		void setInfo(@MaxSize(48) String info);
	}

	public static interface ExpiringVo1 extends ExpiringVo {
		final static int INFO_SIZE = 2;
		final static int MAX_SIZE = 1000 * 300;

		void setInfo(@MaxSize(INFO_SIZE) String info);
	}

	public static interface ExpiringVo48 extends ExpiringVo {
		final static int INFO_SIZE = 48;
		final static int MAX_SIZE = 1000 * 700;

		void setInfo(@MaxSize(INFO_SIZE) String info);
	}

	public static interface ExpiringVo128 extends ExpiringVo {
		final static int INFO_SIZE = 128;
		final static int MAX_SIZE = 1000 * 200;

		void setInfo(@MaxSize(INFO_SIZE) String info);
	}

	public static interface ExpiringVo640 extends ExpiringVo {
		final static int INFO_SIZE = 640;
		final static int MAX_SIZE = 1000 * 300;

		void setInfo(@MaxSize(INFO_SIZE) String info);
	}

}
