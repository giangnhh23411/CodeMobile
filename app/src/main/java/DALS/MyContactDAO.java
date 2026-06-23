package DALS;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

import com.huonggiang.models.Contact;

import java.util.ArrayList;

public class MyContactDAO {

    /** Đọc toàn bộ danh bạ (tên + số điện thoại) từ máy, sắp xếp theo tên. */
    public static ArrayList<Contact> getMyContacts(Context context) {
        ArrayList<Contact> list = new ArrayList<>();

        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String[] projection = {
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
        };
        String sortOrder = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " COLLATE NOCASE ASC";

        try (Cursor cursor = context.getContentResolver()
                .query(uri, projection, null, null, sortOrder)) {
            if (cursor != null) {
                int nameIdx  = cursor.getColumnIndex(
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                int phoneIdx = cursor.getColumnIndex(
                        ContactsContract.CommonDataKinds.Phone.NUMBER);

                while (cursor.moveToNext()) {
                    String name  = cursor.getString(nameIdx);
                    String phone = cursor.getString(phoneIdx);
                    list.add(new Contact(name, phone));
                }
            }
        }
        return list;
    }
}
