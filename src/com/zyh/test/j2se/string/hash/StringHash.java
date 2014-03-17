package com.zyh.test.j2se.string.hash;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * reference:<br>
 * <a
 * href="http://stackoverflow.com/questions/1660501/what-is-a-good-64bit-hash-function-in-java-for-textual-strings">ref1</a><br>
 * <a
 * href="http://www.partow.net/programming/hashfunctions/index.html">ref2</a><br>
 *
 * @author zhyhang
 */
public class StringHash {

    /**
     * one million string, test the collision of hash and time cost APHash can get the good result.
     * @param argv                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              
     */
    public static void main(String... argv) {
        //random generator string
        int cnt = 10000000;
        int strLength = 22;
        int dupCnt = 0;
        long wholeTimeCost = 0;
        // create the char table
        char[] charTable = new char[64];
        int cti = 0;
        for (int i = 0; i < 10; i++) {
            charTable[cti++] = (char) ('0' + i);
        }
        charTable[cti++] = '_';
        for (int i = 0; i < 26; i++) {
            charTable[cti++] = (char) ('a' + i);
        }
        charTable[cti++] = '-';
        for (int i = 0; i < 26; i++) {
            charTable[cti++] = (char) ('A' + i);
        }
        // using index of char table in every position
        int[] usingIndex = new int[strLength];
        int lastIndex = strLength - 1;
        Map<Long, Integer> dupHash = new HashMap<>();
        for (int i = 0; i < cnt; i++) {
            StringBuilder sb = new StringBuilder("CAESE");
            //range the chars at different positions
            for (int j = lastIndex; j > 0; j--) {
                if (usingIndex[j] >= charTable.length) {
                    usingIndex[j] = 0;
                    usingIndex[j - 1]++;
                }
            }
            //append chars
            for (int j = 0; j < strLength; j++) {
                sb.append(charTable[usingIndex[j]]);
            }
            usingIndex[lastIndex]++;
            String s = sb.toString();
//            System.out.println(s);
            //record the time cost
            long ts = System.nanoTime();
            //calculate the string hash
            long hash = GeneralHashFunctionLibrary.CSDNHash(s);
//            long hash = GeneralHashFunctionLibrary.APHash(s);
            wholeTimeCost += System.nanoTime() - ts;
//            System.out.println(hash);
            Integer sameHash = dupHash.get(hash);
            if (null != sameHash) {
                sameHash++;
                dupCnt++;
            } else {
                sameHash = Integer.valueOf(1);
            }
            dupHash.put(hash, sameHash);
        }
        System.out.println("Hash collision counter: [" + dupCnt + "].");
        System.out.println("Calculate [" + cnt + "] string hashcode,cost time (ms) : [" + TimeUnit.NANOSECONDS.toMillis(wholeTimeCost) + "].");
    }

}

//test data
// autohome:
    //C83B66E3-CA6C-3536-A342-D97E89C4EE78||2014-02-08 11:43:41.977||hao.360.cn
