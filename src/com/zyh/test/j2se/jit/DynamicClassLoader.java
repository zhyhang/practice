package com.zyh.test.j2se.jit;

import java.net.URL;
import java.net.URLClassLoader;

public class DynamicClassLoader extends URLClassLoader {
	
    public DynamicClassLoader(ClassLoader parent) {
        super(new URL[0], parent);
    }

    public Class findClassByClassName(String className, JavaClassObject jco)  {
    	Class clazz=null;
    	synchronized(this){
    		clazz=findLoadedClass(className);
    		if(null==clazz){
    			clazz=this.loadClass(className,jco);
    		}
    	}
    	return clazz;
    }

    public Class loadClass(String fullName, JavaClassObject jco) {
        byte[] classData = jco.getBytes();
        return this.defineClass(fullName, classData, 0, classData.length);
    }
    
}
