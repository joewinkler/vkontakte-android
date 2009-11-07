package org.googlecode.vkontakte_android.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
 
import android.util.Log;
                 
public class Phone {
    public static String formatPhoneNumber(String number) {
    	number.replaceAll("[-()]*", "");
        Pattern pattern = Pattern.compile("(\\+?\\d*).*");
        Matcher matcher = pattern.matcher(number);
        return matcher.find() ? matcher.group(1) : null;
    }
}
