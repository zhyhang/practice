/**
 * 
 */
package com.zyh.java8.feature.time;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

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

		// parse local date time
		String dayAfterTommorrow = "20120225";
		LocalDate formatted = LocalDate.parse(dayAfterTommorrow, DateTimeFormatter.BASIC_ISO_DATE);
		System.out.printf("Date generated from String %s is %s %n", dayAfterTommorrow, formatted);
		
		// current millis
		System.out.println("current millis from Clock: "+Clock.systemDefaultZone().millis());
		System.out.println("current millis from System: "+System.currentTimeMillis());	

		// Transfer timestamp to LocalDateTime;
		LocalDateTime ts2dt = Instant.ofEpochMilli(System.currentTimeMillis()).atZone(ZoneId.systemDefault()).toLocalDateTime();
		System.out.println("current millis timestamp to date time: "+ts2dt);
		
		// transfer local date time to millis
		long epochMilli = ts2dt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
		System.out.println("current millis converted from local date time: "+epochMilli);
		
	}

}
