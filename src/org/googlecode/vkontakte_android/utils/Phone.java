package org.googlecode.vkontakte_android.utils;

public class Phone {
    public static String formatPhoneNumber(String number) {
        return number != null ? number.replaceAll("[-()]*", "") : number;
    }
}
