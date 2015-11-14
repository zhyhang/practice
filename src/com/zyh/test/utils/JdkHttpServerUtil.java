/**
 * 
 */
package com.zyh.test.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Jdk embedded http server tool
 * 
 * @author zhyhang
 *
 */
public class JdkHttpServerUtil {
	/**
	 * returns the url parameters in a map
	 * 
	 * @param query
	 * @return map
	 */
	public static Map<String, String> queryToMap(String query) {
		Map<String, String> result = new HashMap<String, String>();
		for (String param : query.split("&")) {
			String pair[] = param.split("=");
			if (pair.length > 1) {
				result.put(pair[0], pair[1]);
			} else {
				result.put(pair[0], "");
			}
		}
		return result;
	}

}
