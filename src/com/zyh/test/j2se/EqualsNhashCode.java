package com.zyh.test.j2se;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class EqualsNhashCode {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Map<SameHashObject, SameHashObject> map=new HashMap<>();
		SameHashObject sho1=new SameHashObject("sho1");
		SameHashObject sho2=new SameHashObject("sho2");
		SameHashObject sho3=new SameHashObject("sho3");
		map.put(sho1, sho1);
		map.put(sho2, sho2);
		map.put(sho3, sho3);
		System.out.println(map.get(new SameHashObject("sho3")));
		System.out.println(map.get(new SameHashObject("sho2")));
		System.out.println(map.get(new SameHashObject("sho1")));
		Set<SameHashObject> shoSet=new HashSet<>();
		shoSet.add(sho1);
		shoSet.add(sho2);
		shoSet.add(sho3);
		shoSet.add(new SameHashObject("sho3"));
		shoSet.add(new SameHashObject("sho2"));
		shoSet.add(new SameHashObject("sho1"));
		System.out.println(shoSet);
		//will get mistake output when equals() redefinition but hashCode() being default.
	}
	
	private static class SameHashObject{
		
		private String name;

		SameHashObject(String name){
			this.name=name;
		}
		
//		public int hashCode(){
//			return name.hashCode();
//		}
		
		public boolean equals(Object o){
//			return this.hashCode()==o.hashCode();
			return this.name.equals(((SameHashObject)o).name);
//			return true;
		}
		
		public String toString(){
			return this.name;
		}
		
		
	}

}
