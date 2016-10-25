/**
 * 
 */
package com.zyh.java8.feature.time;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author zhyhang
 *
 */
public class DateTimeClock {

	public static void main(String[] args) {

		// Date and time with timezone in Java 8 ZoneId america =
		// ZoneId.of("America/New_York");
		LocalDateTime localtDateAndTime = LocalDateTime.now();
		ZonedDateTime dateAndTimeInNewYork = ZonedDateTime.of(localtDateAndTime, ZoneId.of("America/New_York"));
		System.out.println("Current date and time in a local timezone : " + localtDateAndTime);
		System.out.println("Current date and time in a particular timezone : " + dateAndTimeInNewYork);

		String dayAfterTommorrow = "20120225";
		LocalDate formatted = LocalDate.parse(dayAfterTommorrow, DateTimeFormatter.BASIC_ISO_DATE);
		System.out.printf("Date generated from String %s is %s %n", dayAfterTommorrow, formatted);

		// Transfer timestamp to LocalDateTime;
		LocalDate date = Instant.ofEpochMilli(System.currentTimeMillis()).atZone(ZoneId.systemDefault()).toLocalDate();
	}

}
