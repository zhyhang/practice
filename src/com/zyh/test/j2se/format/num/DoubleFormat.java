package com.zyh.test.j2se.format.num;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class DoubleFormat {
	
	/**
	 * 提高性能的处理，预置好前导零
	 */
	public final static char[][] LEADING_DECIMALS=new char[][]{
		"0.".toCharArray(),
		"0.0".toCharArray(),
		"0.00".toCharArray(),
		"0.000".toCharArray(),
		"0.0000".toCharArray(),
		"0.00000".toCharArray(),
		"0.000000".toCharArray(),
		"0.0000000".toCharArray(),
		"0.00000000".toCharArray(),
		"0.000000000".toCharArray(),
		"0.0000000000".toCharArray(),
		"0.00000000000".toCharArray(),
		"0.000000000000".toCharArray(),
		"0.0000000000000".toCharArray(),
		"0.00000000000000".toCharArray(),
		"0.000000000000000".toCharArray()
	};

	/**
	 * @param args
	 */
	public static void main0(String[] args) {
		NumberFormat f = NumberFormat.getNumberInstance();
		f.setGroupingUsed(false);
		f.setMaximumFractionDigits(4);
		System.out.println(f.getMaximumFractionDigits());
		System.out.println(f.getMinimumFractionDigits());
		System.out.println(f.getMaximumIntegerDigits());
		System.out.println(f.getMinimumIntegerDigits());
		
		double initdbl=0.0001d;
		double d = initdbl;
		int calcCount = 1000000;
		String[] fDs = new String[calcCount];
		String[] bdDs = new String[calcCount];
		String[] ffDs = new String[calcCount];
		long ts = System.nanoTime();
		 for (int i = 0; i < calcCount; i++) {
//		  d += 0.0001d;
		 fDs[i] = f.format(d);
		 }
		long ts1 = System.nanoTime();
		d = initdbl;
		for (int i = 0; i < calcCount; i++) {
//			 d += 0.0001d;
			bdDs[i] = bigDecFormat(d, 4);
		}
		long ts2 = System.nanoTime();
		d = initdbl;
		for (int i = 0; i < calcCount; i++) {
//			 d += 0.0001d;
			ffDs[i] = fastFormat(d, 4);
		}
		long ts3 = System.nanoTime();
		System.out.println("NumberFormat time cost: " + TimeUnit.NANOSECONDS.toMillis(ts1 - ts));
		System.out.println("BigDecimal time cost: " + TimeUnit.NANOSECONDS.toMillis(ts2 - ts1));
		System.out.println("Fast Format time cost: " + TimeUnit.NANOSECONDS.toMillis(ts3 - ts2));
		System.out.println(f.format(988881000.0015789d));
		System.out.println(f.format(988881000.12d));
		System.out.println(f.format(-988881000.12d));
		System.out.println(f.format(0.00015d));
		System.out.println(new BigDecimal(1000.0015d - 1000));
		System.out.println(new BigDecimal(Double.toString(0)).stripTrailingZeros());
		System.out.println(BigDecimal.ZERO.stripTrailingZeros());

	}

	/**
	 * Will lost precision, please do not use it!!!
	 * 
	 * @param d
	 * @param precision
	 * @return
	 */
	@Deprecated
	private static String fastFrmError(double d, int precision) {
		int posPrecison = precision;
		if (precision < 0) {
			posPrecison = -precision;
		}
		double roundUpVal = d;
		if (roundUpVal > 0d) {
			roundUpVal += 5d * Math.pow(10d, -(posPrecison + 1));
		} else if (roundUpVal < 0d) {
			roundUpVal -= 5d * Math.pow(10d, -(posPrecison + 1));
		}
		long longPart = (long) roundUpVal;
		if (posPrecison > 0) {
			long longDecimalPart = Math.abs((long) ((roundUpVal - longPart) * Math.pow(10d, posPrecison)));
			String longDecimalPartStr = Long.toString(longDecimalPart);
			String longPartStr = Long.toString(longPart).concat(".");
			for (int i = 0; i < posPrecison - longDecimalPartStr.length(); i++) {
				longPartStr = longPartStr.concat("0");
			}
			return longPartStr.concat(longDecimalPartStr);
		}
		return Long.toString(longPart);
	}
	
	/**
	 * 快速格式化一个double，小数尾零去除<br>
	 * 等同于:<br>
	 * NumberFormat f = NumberFormat.getNumberInstance();<br>
	 * f.setGroupingUsed(false);<br>
	 * f.setMaximumFractionDigits(precision);<br>
	 * f.format(d);<br>
	 * 但一般情况效率高于NumberFormat一倍，且精度无丢失。<br>
	 * 
	 * @param d
	 *            the double value
	 * @param precision
	 *            [0,16]
	 * @return
	 * @see NumberFormat
	 */
	private static String fastFormat(double d, int precision) {
		int posPrecision = Math.abs(precision);
		double roundUpVal = Math.abs(d) * Math.pow(10d, posPrecision) + 0.5d;
		if(roundUpVal>999999999999999d || posPrecision>16){//double has max 16 precisions
			return bigDecFormat(d, posPrecision);
		}
		long longPart = (long)Math.nextUp(roundUpVal);
		if (longPart < 1) {
			return "0";
		}
		char[] longPartChars = Long.toString(longPart).toCharArray();
		char[] formatChars = null;
		if (longPartChars.length > posPrecision) {
			int end = longPartChars.length - 1;
			int decIndex=longPartChars.length-posPrecision;
			while (end >= decIndex && longPartChars[end] == '0') {
				end--;
			}
			if(end >= decIndex){
				formatChars=new char[end+2];
				System.arraycopy(longPartChars, 0, formatChars, 0, decIndex);
				formatChars[decIndex]='.';
				System.arraycopy(longPartChars, decIndex , formatChars, decIndex+1, end - decIndex +1);
			}else{
				formatChars=new char[decIndex];
				System.arraycopy(longPartChars, 0, formatChars, 0, decIndex);
			}
		} else {
			int end = longPartChars.length - 1;
			while (end >= 0 && longPartChars[end] == '0') {
				end--;
			}
			char[] leadings=LEADING_DECIMALS[posPrecision - longPartChars.length];
			formatChars=Arrays.copyOf(leadings,leadings.length+end+1);
			System.arraycopy(longPartChars, 0, formatChars, leadings.length, end+1);
		}
		return Math.signum(d) > 0 ? new String(formatChars) : "-"+new String(formatChars);
	}

	private static String bigDecFormat(double d, int precision) {
			String formatStr=new BigDecimal(Double.toString(d)).setScale(Math.abs(precision), RoundingMode.HALF_UP).toString();
			if(precision==0) {
				return formatStr;
			}
			int end = formatStr.length() -1;
			while (end >= 0 && formatStr.charAt(end) == '0') {
				end--;
			}
			formatStr = formatStr.substring(0, end + 1);
			if(formatStr.charAt(formatStr.length() - 1)=='.'){
				formatStr=formatStr.substring(0,formatStr.length()-1);
			}
			return formatStr;
	}
	public static final BigDecimal[] ORIGINAL_NUMBERS=new BigDecimal[]{
		new BigDecimal(Math.PI), new BigDecimal(-Math.PI), new BigDecimal(Math.E), new BigDecimal(-Math.E), new BigDecimal("1000.0015"),
		new BigDecimal("-1000.0015"), BigDecimal.TEN,BigDecimal.ONE,new BigDecimal("0"), new BigDecimal("1234.9995"),
		new BigDecimal("-1234.9995"),new BigDecimal("998.8995"),new BigDecimal("-998.8995"),new BigDecimal("999.9995"),new BigDecimal("-999.9995"),
		new BigDecimal("0.0015"),new BigDecimal("-0.0015"),new BigDecimal("0.0195"),new BigDecimal("-0.0195"),new BigDecimal("3.22445"),
		new BigDecimal("-3.22445"),new BigDecimal("88888888888.999999945"),new BigDecimal("-88888888888.999499945"),
		new BigDecimal("10.999954"),new BigDecimal("-10.999954"),new BigDecimal("0.0001"),new BigDecimal("-0.0001"),
		new BigDecimal("0.0000001"),new BigDecimal("-0.0000001")
	};
	
	
	public static void main1(String[] argv){
		double[] values = new double[] { Math.PI, -Math.PI, Math.E, -Math.E, 1000.0015d, -1000.0015d, 0d, 1234.9995d,
				-1234.9995d,998.8995d,-998.8995d,999.9995d,-999.9995d,0.0015d,-0.0015d,0.0195d,-0.0195d,3.22445d,-3.22445d,
				88888888888.999499945d,-88888888888.999499945d,10.999954d,-10.999954d,0.0001d,-0.0001d};	
		NumberFormat f = NumberFormat.getNumberInstance();
		f.setGroupingUsed(false);
		f.setMaximumFractionDigits(4);
		System.out.println(f.format(1000.0015d));
		System.out.println(1000.0015d - 1000.0005);
		System.out.println(new BigDecimal("1.1").setScale(30, RoundingMode.HALF_UP).toString());
		System.out.println(Math.ulp(0.0d));
		
		for (double d : values) {
			System.out.print(f.format(d));
			System.out.print("\t\t");
			System.out.print(fastFormat(d, 4));
			System.out.print("\t\t");			
			System.out.println(bigDecFormat(d, 4));
		}
	}
	
	public static void main2(String[] argv){
		//function test
		int testCounter=100000;
		BigDecimal[] values=new BigDecimal[ORIGINAL_NUMBERS.length+testCounter];
		System.arraycopy(ORIGINAL_NUMBERS, 0, values, 0, ORIGINAL_NUMBERS.length);
		values[ORIGINAL_NUMBERS.length]=new BigDecimal("0.0001");
		for (int j = 1; j < testCounter; j++) {
			values[ORIGINAL_NUMBERS.length+j]=values[ORIGINAL_NUMBERS.length+j - 1].add(new BigDecimal("0.0000123456"));
		}
		for (int i = 0; i < 9; i++) {
			NumberFormat f = NumberFormat.getNumberInstance();
			f.setGroupingUsed(false);
			f.setMaximumFractionDigits(i);
			System.out.println("Precision-"+String.valueOf(i)+"\t\t\tOriginal number\t\t\tNumberFormat\t\t\tFastFormat\t\t\tBigDecimalFormat");
			for (BigDecimal bd : values) {
				double d=bd.doubleValue();
				String nf=f.format(d);
				String ff=fastFormat(d, i);
				String bgf=bigDecFormat(d, i);
				if(!ff.equals(bgf)){
//					if(!nf.equals(ff) || !ff.equals(bgf) || !nf.equals(bgf)){
					System.out.print(i);
					System.out.print("\t\t\t");
					System.out.print(bd.toPlainString());
					System.out.print("\t\t\t");
					System.out.print(nf);
					System.out.print("\t\t\t");
					System.out.print(ff);
					System.out.print("\t\t\t");			
					System.out.println(bgf);
				}
			}
		}
	}
	
	public static void main3(String[] argv){
		//performace test
		int testCounter=1000000;
		BigDecimal[] bdValues=new BigDecimal[ORIGINAL_NUMBERS.length+testCounter];
		System.arraycopy(ORIGINAL_NUMBERS, 0, bdValues, 0, ORIGINAL_NUMBERS.length);
		bdValues[ORIGINAL_NUMBERS.length]=new BigDecimal("0.0001");
		for (int j = 1; j < testCounter; j++) {
			bdValues[ORIGINAL_NUMBERS.length+j]=bdValues[ORIGINAL_NUMBERS.length+j - 1].add(new BigDecimal("0.0000123456"));
		}
		double[] values=new double[bdValues.length];
		for (int j = 0; j < values.length; j++) {
			values[j]=bdValues[j].doubleValue();
		}
		for (int i = 0; i < 9; i++) {
			NumberFormat f = NumberFormat.getNumberInstance();
			f.setGroupingUsed(false);
			f.setMaximumFractionDigits(i);
			long ts=System.nanoTime();
			for (double d:values) {
				f.format(d);
			}
			long tsf=System.nanoTime();
			for (double d:values) {
				fastFormat(d, i);
			}
			long tsff=System.nanoTime();
			for (double d:values) {
				bigDecFormat(d, i);
			}
			long tsbgf=System.nanoTime();
			System.out.println("Precision-"+String.valueOf(i)+" and Loop times-"+String.valueOf(testCounter)+":");
			System.out.println("NumberFormat time cost(ms): " + TimeUnit.NANOSECONDS.toMillis(tsf - ts));
			System.out.println("Fast Format time cost(ms):  " + TimeUnit.NANOSECONDS.toMillis(tsff - tsf));
			System.out.println("BigDecimal time cost(ms): " + TimeUnit.NANOSECONDS.toMillis(tsbgf - tsff));
		}
	}
	
	//this is a performace test
	public static void main(String[] argv){
		//performace test
		int testCounter=100000;
		for (int i = 0; i < 9; i++) {
			NumberFormat f = NumberFormat.getNumberInstance();
			f.setGroupingUsed(false);
			f.setMaximumFractionDigits(i);
			long ts=System.nanoTime();
			for (int j = 0; j < testCounter; j++) {
				for (BigDecimal bd:ORIGINAL_NUMBERS) {
					f.format(bd.doubleValue());
				}
			}

			long tsf=System.nanoTime();
			for (int j = 0; j < testCounter; j++) {
				for (BigDecimal bd:ORIGINAL_NUMBERS) {
					fastFormat(bd.doubleValue(), i);
				}
			}
			long tsff=System.nanoTime();
			for (int j = 0; j < testCounter; j++) {
				for (BigDecimal bd:ORIGINAL_NUMBERS) {
					bigDecFormat(bd.doubleValue(), i);
				}
			}
			
			long tsbgf=System.nanoTime();
			System.out.println("Precision-"+String.valueOf(i)+" and Loop times-"+String.valueOf(testCounter)+":");
			System.out.println("NumberFormat time cost(ms): " + TimeUnit.NANOSECONDS.toMillis(tsf - ts));
			System.out.println("Fast Format time cost(ms):  " + TimeUnit.NANOSECONDS.toMillis(tsff - tsf));
			System.out.println("BigDecimal time cost(ms): " + TimeUnit.NANOSECONDS.toMillis(tsbgf - tsff));
		}
	}
	
}
