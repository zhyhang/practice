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

import org.python.util.PythonInterpreter;

/**
 * 
 * @author zhyhang
 */
public class RunJython {
	public final static String LANG_SNIPPET = "import sys\n" +
			"sum=0\n"+
			"for i in range(1,10000):sum=sum+i\n" + 
			"javaObj.setText('jyphon')";

	public static void main(String... argv) {
		System.out.println("run with engine:");
		runWithEngine();
		System.out.println("run with interpreter:");
		runWithInterperet();
	}

	private static void runWithEngine() {
		try {
			ScriptEngineManager sm = new ScriptEngineManager();
			ScriptEngine engine = sm.getEngineByName("jython");
			JButton btn = new JButton("测试按钮");
			engine.put("javaObj", btn);
			long ts = System.currentTimeMillis();
			Object ret = engine.eval(LANG_SNIPPET);
			long ts1 = System.currentTimeMillis();
			ret = engine.eval(LANG_SNIPPET);
			long ts2 = System.currentTimeMillis();
			System.out.println(btn.getText());
			System.out.println(ret);
			System.out.println(ts1 - ts);
			System.out.println(ts2 - ts1);
		} catch (ScriptException ex) {
			Logger.getLogger(RunJython.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
	private static void runWithInterperet(){
        // Create an instance of the PythonInterpreter
        PythonInterpreter interp = new PythonInterpreter();
        // Obtain the value of an object from the PythonInterpreter and store it
        // into a PyObject.
		JButton btn = new JButton("测试按钮");
		interp.set("javaObj", btn);
		long ts = System.currentTimeMillis();
		interp.exec(LANG_SNIPPET);
		long ts1 = System.currentTimeMillis();
		interp.exec(LANG_SNIPPET);
		long ts2 = System.currentTimeMillis();
		System.out.println(btn.getText());
		System.out.println(ts1 - ts);
		System.out.println(ts2 - ts1);
		
	}

}
