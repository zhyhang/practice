/**
 * 
 */
package com.zyh.java8.feature.funinterface;

import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * @author zhyhang
 *
 */
public class FunctionInters {

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		// optional
		Optional<String> name = Optional.of("zhao");
		System.out.println(name.orElse("luan"));
		System.out.println(Arrays.asList("1a", "1b", "1c").stream().allMatch(s -> s.startsWith("1")));

		// file
		Path path = Paths.get("name.txt");
		try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(path))) {
			writer.println("hello");
			writer.println(" world!");
		} catch (Exception e) {
		}
		try (Stream<String> stream = Files.lines(Paths.get("name.txt"))) {
			stream.map(String::trim).forEach(System.out::println);
		} catch (Exception e) {
		}
		System.out.println(Paths.get("name.txt").toFile().getCanonicalPath());

		// map
		ConcurrentHashMap<Integer, String> map = new ConcurrentHashMap<Integer, String>();
		IntStream.range(1, 100).forEach(i -> {
			map.put(i, "no.".concat(Objects.toString(i)));
		});
		map.forEach(1, (key, value) -> System.out.printf("key: %s; value: %s; thread: %s\n", key, value,
				Thread.currentThread().getName()));

		String result = map.reduce(1, (key, value) -> {
			return key + "=" + value;
		} , (s1, s2) -> {
			return s1 + ", " + s2;
		});

		System.out.println("Result: " + result);

		// Optional usage for avoiding null check
		Child child = new Child();
		child.setParent(new Parent());
		Optional.ofNullable(child).map(Child::getParent).ifPresent(System.out::println);

	}

}

class Parent {

	public String toString() {
		return "Children's parent";
	}

}

class Child {

	private Parent parent;

	public Parent getParent() {
		return this.parent;
	}

	public void setParent(Parent parent) {
		this.parent = parent;
	}
}
