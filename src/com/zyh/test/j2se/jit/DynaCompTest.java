package com.zyh.test.j2se.jit;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.swing.JButton;
import javax.tools.JavaFileObject.Kind;

public class DynaCompTest {
	private final static DynamicClassLoader cl=new DynamicClassLoader(DynaCompTest.class.getClassLoader());
	
    public static void main(String[] args) throws Exception {
        String fullName = "com.ipinyou.optimus.plugin.DynaClass";
        StringBuilder src = new StringBuilder();
        src.append("package com.ipinyou.optimus.plugin;\n");
        src.append("public class DynaClass implements com.zyh.test.j2se.jit.DynaCompTest.Embedded{\n");
        src.append("    public void process(javax.swing.JButton btn) {\n");
        src.append("        int sum=0;\n");
        src.append("        for(int i=0;i<10000;i++){sum+=i;}\n");
        src.append("        if(null!=btn){btn.setText(\"Custom Change\");}\n");
        src.append("    }\n");
        src.append("    public String toString() {\n");
        src.append("        int sum=0;\n");
        src.append("        for(int i=0;i<10000;i++){sum+=i;}\n");
        src.append("        return \"Hello, I am \" + ");
        src.append("this.getClass().getSimpleName()+sum+\"(10000)\";\n");
        src.append("    }\n");
        src.append("}\n");

        System.out.println(src);
        DynamicEngine de = DynamicEngine.getInstance();
        Object[] rets =  de.javaCodeToObject(fullName,src.toString());
        byte[] clazzMeta=((JavaClassObject) rets[0]).getBytes();
        /*
        String anotherSrc=src.toString().replaceAll("10000", "20000");
        byte[] anotherClazzMeta=((JavaClassObject) de.javaCodeToObject(fullName,anotherSrc)[0]).getBytes();
        Class<?> anotherClazz = loadClass(fullName, anotherClazzMeta);
        System.out.println(anotherClazz.newInstance().toString());
        */
        JButton btn = new JButton("Original Text");
        long ts=System.nanoTime();
        ((Embedded)rets[1]).process(btn);
        long ts1=System.nanoTime();
        Class<?> clazz = loadClass(fullName, clazzMeta);
        com.zyh.test.j2se.jit.DynaCompTest.Embedded instance = (Embedded) clazz.newInstance();
        instance.process(btn);
        long ts2=System.nanoTime();
        instance = (Embedded) clazz.newInstance();
        instance.process(btn);
        long ts3=System.nanoTime();
        System.out.println(btn.getText());
        System.out.println((ts1-ts)+"ns");
        System.out.println((ts2-ts1)+"ns");
        System.out.println((ts3-ts2)+"ns");
//        System.out.println(clazz.newInstance().toString());
        TimeUnit.SECONDS.sleep(15);
        Map<Integer,Class<?>> classMap=new HashMap<Integer,Class<?>>(); 
        for (int i = 0; i < 100000; i++) {
        	clazz = loadClass(fullName, clazzMeta);
//        	classMap.put(Integer.valueOf(i), clazz);
        	//unload class please refer http://stackoverflow.com/questions/148681/unloading-classes-in-java
		}
        System.out.println("load "+classMap.size()+" classes!");
        TimeUnit.MINUTES.sleep(5);
    }
    
    public interface Embedded{
    	void process(javax.swing.JButton btn) throws Exception;
    }
    
    private static Class<?> loadClass(String fullName,byte[] classBin){
        JavaClassObject jco=new JavaClassObject(fullName,Kind.CLASS);
        try {
			jco.openOutputStream().write(classBin);
		} catch (IOException e) {
			return null;
		}
        //jdk1.7下各种垃圾回收都会回收PermGen
        return new DynamicClassLoader(DynaCompTest.class.getClassLoader()).loadClass(fullName, jco);
//        return cl.findClassByClassName(fullName,jco);
    }
    
}
