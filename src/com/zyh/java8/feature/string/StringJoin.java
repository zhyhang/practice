/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zyh.java8.feature.string;

import static java.lang.System.out;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Learning String join at JDK8
 *
 * @author zhyhang
 */
public class StringJoin {

	public static void main(String... argv) {
		joiningArray();
		joiningStreamWithPreSuf();
	}

	/**
	 * Words associated with the blog at http://marxsoftware.blogspot.com/ in
	 * array.
	 */
	private final static String[] blogWords = { "Inspired", "by", "Actual", "Events" };

	/**
	 * Demonstrate joining multiple Strings using static String "join" method
	 * that accepts a "delimiter" and a variable number of Strings (or an array
	 * of Strings).
	 */
	private static void joiningArray() {
		final String blogTitle = String.join(" ", blogWords);
		out.println("Blog Title: " + blogTitle);

		final String postTitle = String.join(" ", "Joining", "Strings", "in", "JDK", "8");
		out.println("Post Title: " + postTitle);
	}

	/**
	 * Demonstrate joining Strings in a collection via that collection's Stream
	 * and use of a Joining Collector that with specified prefix and suffix.
	 */
	private static void joiningStreamWithPreSuf() {
		final List<String> stringsToJoin = Arrays.asList("216", "58", "216", "206");
		final String ipAddress = stringsToJoin.stream().collect(Collectors.joining(".", "(", ")"));
		out.println("IP Address: " + ipAddress);
	}

}
