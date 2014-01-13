/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zyh.test.j2se;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;

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
public class RunGroovy {
	public final static String LANG_SNIPPET = "//println 'Hello, Groovy!'\n" + "def sum=0\n"
			+ "for(i=0;i<10000;i++){sum=sum+i}\n" + "//println javaObj.getText()\n" + "javaObj.setText('groovy')";

	public final static String LANG_SNIPPET1 = "def sum=0\n" + "for(i=0;i<10000;i++){sum=sum+i}\n"
			+ "javaObj.setText('groovy')";

	public static void main(String... argv) {
		System.out.println("run with engine:");
		runWithEngine();
		System.out.println("run with shell:");
		runWithShell();
	}

	private static void runWithEngine() {
		try {
			ScriptEngineManager sm = new ScriptEngineManager();
			ScriptEngine engine = sm.getEngineByName("groovy");
			JButton btn = new JButton("测试按钮");
			engine.put("javaObj", btn);
			long ts = System.currentTimeMillis();
			Object jsReturn = engine.eval(LANG_SNIPPET);
			long ts1 = System.currentTimeMillis();
			jsReturn = engine.eval(LANG_SNIPPET1);
			long ts2 = System.currentTimeMillis();
			System.out.println(btn.getText());
//			System.out.println(jsReturn);
			System.out.println(ts1 - ts);
			System.out.println(ts2 - ts1);
		} catch (ScriptException ex) {
			Logger.getLogger(RunGroovy.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
	private static void runWithShell(){
		Binding binding = new Binding();
		JButton btn = new JButton("测试按钮");
		long ts = System.currentTimeMillis();
		binding.setVariable("javaObj", btn);
		GroovyShell shell = new GroovyShell(binding);
		shell.evaluate(LANG_SNIPPET);
		long ts1 = System.currentTimeMillis();
		binding.setVariable("javaObj", btn);
		shell = new GroovyShell(binding);
		shell.evaluate(LANG_SNIPPET1);
		long ts2 = System.currentTimeMillis();
		System.out.println(btn.getText());
		System.out.println(ts1 - ts);
		System.out.println(ts2 - ts1);
		
	}

}
