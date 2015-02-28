/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zyh.java8.feature.string;

import static java.lang.System.out;

/**
 * Learning String join at JDK8
 *
 * @author zhyhang
 */
public class StringJoin {

    public static void main(String... argv) {
        joiningArray();
    }

    /**
     * Words associated with the blog at http://marxsoftware.blogspot.com/ in
     * array.
     */
    private final static String[] blogWords = {"Inspired", "by", "Actual", "Events"};

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

}
