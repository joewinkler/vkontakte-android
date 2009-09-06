package org.googlecode.vkontakte_android.matching;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Contacts;
import org.googlecode.vkontakte_android.contacts.ContactsHelper;

public class ContactsMatcher {
    private final ContentResolver contentResolver;

    public ContactsMatcher(Context context) {
        contentResolver = context.getContentResolver();
    }

    public Uri findPersonByPhones(String... phones) {

        String[] filtered = new String[phones.length];

        StringBuilder sb = new StringBuilder();
        for (int i = 0, j = 0, phonesLength = phones.length; i < phonesLength; i++) {

            String ph = phones[i].replaceAll("[^\\d\\+]+", "");
            if (ph.length() < 6) continue;

            if (ph.startsWith("8")) {
                ph = "+7" + ph.substring(1);
            }

            filtered[j++] = ph;
            if (j != 0) sb.append(" or ");
            sb.append(Contacts.PhonesColumns.NUMBER_KEY + "=?");
        }

        final Cursor q = contentResolver.query(Contacts.Phones.CONTENT_URI, new String[]{"_id"},
                sb.toString(), filtered, null);

        try {

            if (!q.moveToFirst()) return null;
            return ContactsHelper.createPersonUri(q.getString(q.getColumnIndexOrThrow("_id")));

        } finally {
            q.close();
        }
    }

}
