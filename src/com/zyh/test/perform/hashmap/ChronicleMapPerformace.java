/**
 * 
 */
package com.zyh.test.perform.hashmap;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.openhft.chronicle.map.ChronicleMap;
import net.openhft.chronicle.map.ChronicleMapBuilder;
import net.openhft.lang.model.constraints.MaxSize;

/**
 * ChronicleMap performace check
 * @author zhyhang
 *
 */
public class ChronicleMapPerformace {
	
	private static ChronicleMap<String,ExpiringVo48> cm = null;
	
	private static ChronicleMap<String,ExpiringVo128> cm_128 = null;
	
	private static ChronicleMap<String,ExpiringVo640> cm_640 = null;
	
	private static ChronicleMap<String,ExpiringVo1> cm_1 = null;
	
	private static Logger logger = LoggerFactory.getLogger(ChronicleMapPerformace.class);
	
	public static void main(String... argv) throws Exception{
		long tsb=System.currentTimeMillis();
		cm=initMap("cm_800K_48Len.cache",ExpiringVo48.class,1000 * 800);
		cm_128=initMap("cm_300K_128Len.cache",ExpiringVo128.class,1000 * 300);
		cm_640=initMap("cm_300K_640Len.cache",ExpiringVo640.class,1000 * 300);
		cm_1=initMap("cm_100K_1Len.cache",ExpiringVo1.class,1000 * 100);
		System.out.format("initMap cost: %dms.\n",System.currentTimeMillis() - tsb);
		Thread.sleep(10000);
		tsb=System.currentTimeMillis();
		fillMap(cm,ExpiringVo48.SIZE - 1);
		fillMap(cm_128,ExpiringVo128.SIZE - 1);
		fillMap(cm_640,ExpiringVo640.SIZE - 2);
		fillMap(cm_1,ExpiringVo1.SIZE - 1);
		System.out.format("fillMap cost: %dms.\n",System.currentTimeMillis() - tsb);
		System.out.format("access counter: %d.\n",cm.entrySet().iterator().next().getValue().getAccessCounter());
		tsb=System.currentTimeMillis();
		readMap(cm);
		readMap(cm_128);
		readMap(cm_640);
		readMap(cm_1);
		System.out.format("readMap cost: %dms.\n",System.currentTimeMillis() - tsb);		
		cm.close();
		Thread.sleep(10000);
	}
	
	private static <T extends ExpiringVo> ChronicleMap<String, T> initMap(String fileName,Class<T> valueClazz,int entrySize){		
		final String basePath = System.getProperty("java.io.tmpdir") + File.separator + "test";
		File cacheFile = new File(basePath + File.separator + fileName);
		try {
			return ChronicleMapBuilder.of(String.class, valueClazz)
					.entries(entrySize).createPersistedTo(cacheFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private static <T extends ExpiringVo> void fillMap(ChronicleMap<String,T> rcm,int infoSize){
		if(rcm!=null && rcm.size()>0){
			return; //ignore
		}
		int i=0;
		for (;;) {
			String key="u_**********_**********_**********_".concat(String.valueOf(i++));
			T vo = rcm.newValueInstance();
			String info=RandomStringUtils.randomAlphanumeric(infoSize);
			vo.setInfo(info);
			vo.setTime((int) (System.currentTimeMillis() % Integer.MAX_VALUE));
			try{
				rcm.put(key, vo);
			}catch(Exception e){
				logger.info("Error in put entry at "+vo.getClass().getSimpleName(),e);
				break; // over the size
			}
		}
	}
	
	private static <T extends ExpiringVo> void readMap(ChronicleMap<String,T> rcm){
		for (String key:rcm.keySet()) {
			ExpiringVo vo = rcm.get(key);//cm.getUsing(key, usingVo);
			int counter=vo.getAccessCounter();
			counter = counter<Integer.MAX_VALUE?counter+1:counter;
			vo.setAccessCounter(counter);
			vo.getInfo();
		}
	}
	
	public static interface ExpiringVo{
		int getTime();
		void setTime(int ts);
		int getAccessCounter();
		void setAccessCounter(int counter);
		String getInfo();
		void setInfo(String info);
	}
	
	public static interface ExpiringVo1 extends ExpiringVo{
		final static int SIZE=2;
		void setInfo(@MaxSize(SIZE) String info);
	}
	
	public static interface ExpiringVo48 extends ExpiringVo{
		final static int SIZE=48;
		void setInfo(@MaxSize(SIZE) String info);
	}
	
	public static interface ExpiringVo128 extends ExpiringVo{
		final static int SIZE=128;
		void setInfo(@MaxSize(SIZE) String info);
	}
	
	public static interface ExpiringVo640 extends ExpiringVo{
		final static int SIZE=640;
		void setInfo(@MaxSize(SIZE) String info);
	}

}
