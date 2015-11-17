/**
 * 
 */
package com.zyh.test.chronicle;

import java.io.File;
import java.io.IOException;

import net.openhft.chronicle.map.ChronicleMap;
import net.openhft.chronicle.map.ChronicleMapBuilder;
import net.openhft.chronicle.map.WriteContext;
import net.openhft.lang.model.DataValueClasses;

/**
 * check if safety using thread local <pre>DataValueClasses.newDirectInstance(xxx.class);</pre>
 * <br><b> It's Ok. But using threadlocal direct instance with <pre>try (WriteContext context = map.acquireUsingLocked(...)) { directInstance update code...}</pre> </b>
 * 
 * @author zhyhang
 *
 */
public class ChronicleMapDirectInstance {
	
	private final static ThreadLocal<Values> threadCache = new ThreadLocal<Values>(){

		protected Values initialValue() {
			return DataValueClasses.newDirectInstance(Values.class);
		}
		
	};

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		File f = File.createTempFile("c-map-direct", ".dat");
		f.deleteOnExit();
		ChronicleMapBuilder<String, Values> mapBuilder = ChronicleMapBuilder
				.of(String.class, Values.class).entries(100L);
		ChronicleMap<String, Values> map = mapBuilder.createPersistedTo(f);
		for (int i = 0; i < 10; i++) {
			Values vs=threadCache.get();
			System.out.format("[%d] threadlocal-value[%d]\n",i,vs.getValue());
			try (WriteContext<String, Values> context = map.acquireUsingLocked(String.valueOf(i), vs)) {
				System.out.format("[%d] before-added-value[%d]\n",i,context.value().getValue());
				context.value().addValue(10);
				System.out.format("[%d] after-added-value[%d]\n",i,context.value().getValue());
			}
		}
		map.close();
	}
	
	public static interface Values{
		void setValue(long v);
		long getValue();
		long addValue(long delta);
	}

}
