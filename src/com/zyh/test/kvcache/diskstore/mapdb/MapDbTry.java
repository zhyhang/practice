/**
 * 
 */
package com.zyh.test.kvcache.diskstore.mapdb;

import java.util.concurrent.ConcurrentMap;

import org.mapdb.DB;
import org.mapdb.DBMaker;

/**
 * try the MapDb<br>
 * check storage <br>
 * check serialize/deserialize<br>
 * 
 * @author zhyhang
 *
 */
@SuppressWarnings("unchecked")
public class MapDbTry {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		// create a db using mmap file
		String file = "/data/cache/mapdb/f1.dat";
		DB db = DBMaker.fileDB(file).fileMmapEnable().allocateStartSize(1 * 1024 * 1024 * 1024) // 1GB
				.fileMmapPreclearDisable().allocateIncrement(64 * 1024 * 1024) // 64MB
				.make();
		
		ConcurrentMap<String, String> map1 = (ConcurrentMap<String, String>) db.hashMap("map1").createOrOpen();
		System.out.format("map size: %d.\nhello %s! \n",  map1.size(), map1.get("hello"));
		map1.put("hello", "world");
		for (int i = 0; i < 100000; i++) {
			map1.put("hello"+i, "world!"+i);
		}
		
		db.close();
	}

}
