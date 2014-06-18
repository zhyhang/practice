package com.zyh.test.hppc;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.carrotsearch.hppc.LongObjectMap;
import com.carrotsearch.hppc.LongObjectOpenHashMap;

public class HppcVsHashMap {

	public static void main(String[] args) {
		Random rad=new Random();
		// prepare data
		int size=1000;
		long maxId=800000;
		Entity[] es=new Entity[size];
		for (int i = 0; i < es.length; i++) {
			es[i]=new Entity(Math.abs(rad.nextLong()) % maxId);
		}
		int itsCount=100000;
		
		System.out.println("Map Put iterate times: "+itsCount+".");
		
		
		//test for jdk hashmap
		Map<Long,Entity> hmap = null;
		long ts=System.nanoTime();
		for (int i = 0; i < itsCount; i++) {
			hmap= new HashMap<>();
			for (int j = 0; j < es.length; j++) {
				hmap.put(es[j].getId(), es[j]);
			}
		}
		System.out.println("jdk hash map time costs (ns) :\t\t"+(System.nanoTime() - ts )+".");
		
		//test for hppc put
		LongObjectMap<Entity> loMap=null;
		ts=System.nanoTime();
		for (int i = 0; i < itsCount; i++) {
			loMap = new LongObjectOpenHashMap<>();
			for (int j = 0; j < es.length; j++) {
				loMap.put(es[j].getId().longValue(), es[j]);
			}
		}
		System.out.println("hppc map time costs (ns) :\t\t"+(System.nanoTime() - ts )+".");
		
		System.out.println("Map Get iterate times: "+itsCount+".");
		
		//test for jdk hashmap
		ts=System.nanoTime();
		for (int i = 0; i < itsCount; i++) {
			for (int j = 0; j < es.length; j++) {
				hmap.get(es[j].getId());
			}
		}
		System.out.println("jdk hash map time costs (ns) :\t\t"+(System.nanoTime() - ts )+".");
		
		//test for hppc get
		ts=System.nanoTime();
		for (int i = 0; i < itsCount; i++) {
			for (int j = 0; j < es.length; j++) {
				loMap.get(es[j].getId().longValue());
			}
		}
		System.out.println("hppc map time costs (ns) :\t\t"+(System.nanoTime() - ts )+".");
		


	}
	
	private static class Entity{
		private Long id;
		
		public Entity(Long id){
			this.id = id;
		}
		
		public Long getId(){
			return this.id;
		}
		
	}

}
