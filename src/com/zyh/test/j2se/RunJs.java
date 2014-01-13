/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zyh.test.j2se;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.swing.JButton;

/**
 *
 * @author zhyhang
 */
public class RunJs {
    public final static String LANG_SNIPPET="//var a=10,b=20,c=a+b;\n" +
    		"//print(c);\n" +
    		"//print(javaObj.getText());\n" +
    		"//javaObj.setText('js修改了');\n" +
    		"var sum=0;\n" +
    		"var i=0;\n" +
    		"for(;i<10000;i++){sum+=i;};\n" +
    		"//print(sum);";
    
	public static void main(String... argv) {
		try {
			ScriptEngineManager sm = new ScriptEngineManager();
			ScriptEngine engine = sm.getEngineByName("JavaScript");
			JButton btn = new JButton("测试按钮");
			engine.put("javaObj", btn);
			long ts = System.currentTimeMillis();
			Object jsReturn = engine.eval(LANG_SNIPPET);
			long ts1 = System.currentTimeMillis();
			jsReturn = engine.eval(LANG_SNIPPET);
			long ts2 = System.currentTimeMillis();
			System.out.println(btn.getText());
			System.out.println(jsReturn);
			System.out.println();
			System.out.println(ts1 - ts);
			System.out.println(ts2 - ts1);
		} catch (ScriptException ex) {
			Logger.getLogger(RunJs.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

}
