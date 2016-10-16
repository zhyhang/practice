package com.zyh.java8.feature.lambda;

import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LambdaInstance {

	public static final Integer MAX_LENGTH = 64;
	private static Logger logger = LoggerFactory.getLogger(LambdaInstance.class);
	private String name = "Allan";
	private int age = 33;

	private boolean judgeAge(Integer age) {
		return this.age == age;
	}

	private boolean judgeAgeNoRef(Integer age) {
		return ((int) age) > 30;
	}

	private static boolean judgeAgeNoRefStatic(Integer age) {
		return ((int) age) > 30;
	}

	private Predicate<String> nameJugder = name -> this.name.equals(name);

	private Predicate<Integer> ageJugder = age -> {
		logger.info("age[{}], max length[{}]", age, MAX_LENGTH);
		return this.age == age;
	};

	private Predicate<Integer> ageJugderMethod = this::judgeAge;

	private void instanceCheckName(Predicate<String> judger, String desc) {
		System.out.println(desc + ": " + judger.toString());
	}

	private void instanceCheckAge(Predicate<Integer> judger, String desc) {
		System.out.println(desc + ": " + judger.toString());
	}

	public static void main(String[] args) {
		LambdaInstance lambdaTest = new LambdaInstance();
		int loopc = 2;
		System.out.println("----Only one instance for many times call----");
		// concise lambda, no refer outter var, one instance for many times call
		for (int i = 0; i < loopc; i++) {
			lambdaTest.instanceCheckAge((Integer age) -> age.intValue() >= 33,
					"concise lambda, no refer outter var, one instance for many times call");
		}
		System.out.println();

		// concise lambda, refer constant outter var, one instance for many times call
		for (int i = 0; i < loopc; i++) {
			lambdaTest.instanceCheckName((String name) -> name.length() <= MAX_LENGTH,
					"concise lambda, refer constant outter var, one instance for many times call");
		}
		System.out.println();

		// concise lambda, refer static outter var, one instance for many times call
		for (int i = 0; i < loopc; i++) {
			lambdaTest.instanceCheckAge((Integer age) -> {
				logger.info("");
				return true;
			}, "concise lambda, refer static outter var, one instance for many times call");
		}
		System.out.println();

		// method-static lambda, no refer outter var, one instance for many times call
		for (int i = 0; i < loopc; i++) {
			lambdaTest.instanceCheckAge(LambdaInstance::judgeAgeNoRefStatic,
					"method-static lambda, no refer outter var, one instance for many times call");
		}
		System.out.println();

		// field lambda, although refer outter var, one instance for many times call
		for (int i = 0; i < loopc; i++) {
			lambdaTest.instanceCheckAge(lambdaTest.ageJugder,
					"field lambda although outter var, one instance for many times call");
		}
		System.out.println();

		// field lambda, although refer outter var, one instance for many times call
		for (int i = 0; i < loopc; i++) {
			lambdaTest.instanceCheckName(lambdaTest.nameJugder,
					"field lambda, although outter var, one instance for many times call");
		}
		System.out.println();

		// field lambda, although field refer method with referring outter var, one instance for many times call
		for (int i = 0; i < loopc; i++) {
			lambdaTest.instanceCheckAge(lambdaTest.ageJugderMethod,
					"field lambda, although field refer method with referring outter var, one instance for many times call");
		}
		System.out.println();

		System.out.println("----New instance created for every call----");
		// concise lambda, refer outter var, new instance created for every call
		for (int i = 0; i < loopc; i++) {
			lambdaTest.instanceCheckAge((Integer age) -> lambdaTest.age >= age.intValue(),
					"concise lambda, refer outter var, new instance created for every call");
		}
		System.out.println();

		// concise lambda, refer outter local var, new instance created for every call
		// but you can pre define lambda before many times call, for reducing instance created.
		int lage = 33;
		// Predicate<Integer> lAgeJudger=(Integer age) -> lage >= age.intValue();
		for (int i = 0; i < loopc; i++) {
			lambdaTest.instanceCheckAge((Integer age) -> lage >= age.intValue(),
					"concise lambda, refer outter local var, new instance created for every call, but you can pre define lambda before many times call");
		}
		System.out.println();

		// method lambda, whether or no ref outter var, new instance created for every call
		for (int i = 0; i < loopc; i++) {
			lambdaTest.instanceCheckAge(lambdaTest::judgeAge,
					"method lambda, whether or no ref outter var, new instance created for every call");
		}
		System.out.println();

		// method lambda, whether or no ref outter var, new instance created for every call
		for (int i = 0; i < loopc; i++) {
			lambdaTest.instanceCheckAge(lambdaTest::judgeAgeNoRef,
					"method lambda, whether or no ref outter var, new instance created for every call");
		}
		System.out.println();
	}

}
