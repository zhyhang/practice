/**
 * 
 */
package com.zyh.java8.feature.funinterface;

import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;
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

		Optional<String> name = Optional.of("zhao");
		System.out.println(name.orElse("luan"));

		System.out.println(Arrays.asList("1a", "1b", "1c").stream().allMatch(s -> s.startsWith("1")));

		Path path = Paths.get("name.txt");
		try (BufferedWriter writer = Files.newBufferedWriter(path)) {
			writer.write("hello");
			writer.newLine();
			writer.write(" world!");
		} catch (Exception e) {
		}

		try (Stream<String> stream = Files.lines(Paths.get("name.txt"))) {
			stream.map(String::trim).forEach(System.out::println);
		} catch (Exception e) {
		}
		
		System.out.println(Paths.get("name.txt").toFile().getCanonicalPath());

	}

}
