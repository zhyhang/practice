/**
 * 
 */
package com.zyh.test.perform.hashmap;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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

	private static final String KEY_PREFIX = "u_**********_**********_**********_";

	private static ChronicleMap<String, ExpiringVo48> cm = null;

	private static ChronicleMap<String, ExpiringVo128> cm_128 = null;

	private static ChronicleMap<String, ExpiringVo640> cm_640 = null;

	private static ChronicleMap<String, ExpiringVo1> cm_1 = null;
	
	private static Map<String, ProductExpiringVo> hm = null;

	private static Logger logger = LoggerFactory.getLogger(ChronicleMapPerformace.class);
	
    private static final ThreadLocal<StringBuilder> sbtl = new ThreadLocal<StringBuilder>();

    private static StringBuilder acquireStringBuilder() {
        StringBuilder sb = sbtl.get();
        if (sb == null) {
            sbtl.set(sb = new StringBuilder(64));
        }
        sb.setLength(0);
        return sb;
    }

	public static void main(String... argv) throws Exception {
//		cmPerform();
//		hmMemFootprint(); // 1000 * 1000 entries = 300M memory
		cmConcurrentPerform();
		hmConcurrentPerform();
	}
	
	private static void cmPerform() throws InterruptedException{
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
		for (int i = 0; i < 3; i++) {
			tsb = System.currentTimeMillis();
			readMap(cm, ExpiringVo48.INFO_SIZE);
			readMap(cm_128, ExpiringVo128.INFO_SIZE);
			readMap(cm_640, ExpiringVo640.INFO_SIZE);
			readMap(cm_1, ExpiringVo1.INFO_SIZE);
			System.out.format("%d readMap cost: %dms.\n", i + 1, System.currentTimeMillis() - tsb);
//			Thread.sleep(15000);
		}
		System.gc();
		long mm=Runtime.getRuntime().freeMemory();
		System.out.format("free memory in this JVM process: %d.\n", mm);
		Thread.sleep(30000);
	}
	
	private static void hmMemFootprint() throws InterruptedException{
		hm=new HashMap<>();
		int size = 1000 * 1000 ;
		fillMap(hm, 48, size);
		System.gc();
		long mm=Runtime.getRuntime().freeMemory();
		System.out.format("free memory in this JVM process: %d.\n", mm);
	}
	
	private static void cmConcurrentPerform() throws InterruptedException{
		final List<String> keys=prepareKeys( 1000 * 1000 );
		ExecutorService es=Executors.newFixedThreadPool(200);
		cm = initMap("cm_700K_48Len.cache", ExpiringVo48.class, ExpiringVo48.MAX_SIZE);
		fillMap(cm, ExpiringVo48.INFO_SIZE - 1, ExpiringVo48.MAX_SIZE);
		int times=10;
		final StringBuilder sb=new StringBuilder(ExpiringVo48.MAX_SIZE);
		long tsb=System.currentTimeMillis();
		for (int i = 0; i < times; i++) {
			for (int j = 0; j < keys.size(); j++) {
				final String key = keys.get(j);
				es.execute(new Runnable() {
					@Override
					public void run() {
						 ExpiringVo vo = cm.get(key);
						 if(vo != null ){
							 vo.getInfo();
//							 vo.getUsingInfo(sb);//acquireStringBuilder());
						 }  
					}
				});

			}
		}
		es.shutdown();
		es.awaitTermination(10, TimeUnit.MINUTES);
		System.out.format("Single thread run %s %d keys time cost: %dms.\n", cm.getClass().getSimpleName(),keys.size() * times,System.currentTimeMillis() - tsb);
	}
	
	
	private static void hmConcurrentPerform() throws InterruptedException{
		hm=new ConcurrentHashMap<>();
		fillMap(hm, 48, 1000 * 70);
		List<String> keys=prepareKeys( 1000 * 1000 );
		ExecutorService es=Executors.newFixedThreadPool(200);
		int times=10;
		long tsb=System.currentTimeMillis();
		for (int i = 0; i < times; i++) {
			for (int j = 0; j < keys.size(); j++) {
				 final String key = keys.get(j);
				 es.execute(new Runnable() {
					@Override
					public void run() {
						 ExpiringVo vo = hm.get(key);
						 if(vo != null ){
							 vo.getInfo();
						 }  
					}
				});

			}
		}
		es.shutdown();
		es.awaitTermination(10, TimeUnit.MINUTES);
		System.out.format("Single thread run %s %d keys time cost: %dms.\n", hm.getClass().getSimpleName(), keys.size() * times,System.currentTimeMillis() - tsb);
	}
	
	private static List<String> prepareKeys(int len){
		List<String> keys= new ArrayList<>();
		for (int i = 0; i < len; i++) {
			keys.add(KEY_PREFIX.concat(String.valueOf(i)));
		}
		Collections.shuffle(keys);
		return keys;
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

	private static <T extends ExpiringVo> void fillMap(Map<String, T> rcm, int infoSize, int maxSize) {
		if (rcm != null && rcm.size() > 0) {
			return; // ignore
		}
		for (int i = 0; i < maxSize; i++) {
			String key = KEY_PREFIX.concat(String.valueOf(i));
			T vo = null;
			if(rcm instanceof ChronicleMap){
				vo = (T) ((ChronicleMap) rcm).newValueInstance();
			}else{
				vo =(T) new ProductExpiringVo();
			}
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

	private static <T extends ExpiringVo> void readMap(ChronicleMap<String, T> rcm,int infoSize) {
		// System.out.println(rcm.get("u_**********_**********_**********_8888"));
		for (Map.Entry<String, T> entry:rcm.entrySet()) {
			// T usingVo = rcm.newValueInstance();
			T vo = entry.getValue();//rcm.get(key);// rcm.getUsing(entry.getKey(),
											// usingVo);
			int counter = vo.getAccessCounter();
			counter = counter < Integer.MAX_VALUE ? counter + 1 : counter;
			vo.setAccessCounter(counter);
			vo.setTime(vo.getTime() + 1);
//			vo.getInfo();
			vo.getUsingInfo(new StringBuilder(infoSize));
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
		
		StringBuilder getUsingInfo(StringBuilder stringBuilder);
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
	
	public static class ProductExpiringVo implements ExpiringVo{
		
		private int accessCounter=0;
		
		private int time=0;
		
		private String info;

		@Override
		public int getTime() {
			return time;
		}

		@Override
		public void setTime(int ts) {
			this.time=ts;
		}

		@Override
		public int getAccessCounter() {
			return accessCounter;
		}

		@Override
		public void setAccessCounter(int counter) {
			this.accessCounter=counter;
		}

		@Override
		public String getInfo() {
			return info;
		}

		@Override
		public void setInfo(String info) {
			this.info=info;
		}

		@Override
		public StringBuilder getUsingInfo(StringBuilder stringBuilder) {
			return null;
		}
		
	}

}
