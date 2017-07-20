/**
 * 
 */
package com.zyh.mobile.phone.area;

import java.util.Locale;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.google.i18n.phonenumbers.geocoding.PhoneNumberOfflineGeocoder;


/**
 * @author zhyhang
 *
 */
public class MobilePhoneNumbersArea {
	
	    static PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
	    static PhoneNumberOfflineGeocoder phoneNumberOfflineGeocoder = PhoneNumberOfflineGeocoder.getInstance();

	    public static String parse(String phone) {
	        try {
	            Phonenumber.PhoneNumber referencePhonenumber = phoneUtil.parse(phone, "CN");
	            String code = phoneNumberOfflineGeocoder.getDescriptionForNumber(referencePhonenumber, Locale.CHINA);
	            return code;
	        } catch (NumberParseException e) {
	            e.printStackTrace();
	        }
	        return null;
	    }

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println(parse("13488651540"));
		System.out.println(parse("13677086736"));
		System.out.println(parse("13532558895"));
		System.out.println(parse("15311455887"));
		System.out.println(parse("18603151358"));
		System.out.println(parse("13452840800"));
	}

}
