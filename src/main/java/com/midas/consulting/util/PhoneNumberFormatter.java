package com.midas.consulting.util;

import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

public class PhoneNumberFormatter {

    private static final PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();

    public static String formatPhoneNumber(String phoneNumber) {
        try {
            Phonenumber.PhoneNumber parsedNumber = phoneNumberUtil.parse(phoneNumber, "US");
            return phoneNumberUtil.format(parsedNumber, PhoneNumberUtil.PhoneNumberFormat.NATIONAL);
        } catch (Exception e) {
            return phoneNumber; // Return the original if parsing fails
        }
    }
}
