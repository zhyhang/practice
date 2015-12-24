/**
 * 
 */
package com.zyh.test.logger;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * @author zhyhang
 *
 */
public class LogbackDynamicPath {

	private static final Logger logger = LoggerFactory.getLogger("test_dpath.log");

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		for (int i = 0; i < 1000; i++) {
			final int fi = i;
			new Thread(() -> {
				MDC.put("partner", "xcard" + fi % 2);
				logger.info("test" + fi);
			}).start();
		}
		TimeUnit.SECONDS.sleep(5);
	}

}
