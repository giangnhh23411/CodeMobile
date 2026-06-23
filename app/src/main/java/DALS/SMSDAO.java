package DALS;

import android.content.Context;
import android.database.Cursor;
import android.provider.Telephony;

import com.huonggiang.models.SMS;

import java.util.ArrayList;

public class SMSDAO {

    /** Đọc toàn bộ tin nhắn (đến + đi) trên máy, mới nhất lên đầu. */
    public static ArrayList<SMS> getAllSms(Context context) {
        ArrayList<SMS> list = new ArrayList<>();

        String[] projection = {
                Telephony.Sms.ADDRESS,
                Telephony.Sms.DATE,
                Telephony.Sms.BODY,
                Telephony.Sms.TYPE
        };
        String sortOrder = Telephony.Sms.DATE + " DESC";

        try (Cursor cursor = context.getContentResolver().query(
                Telephony.Sms.CONTENT_URI, projection, null, null, sortOrder)) {

            if (cursor != null) {
                int addrIdx = cursor.getColumnIndex(Telephony.Sms.ADDRESS);
                int dateIdx = cursor.getColumnIndex(Telephony.Sms.DATE);
                int bodyIdx = cursor.getColumnIndex(Telephony.Sms.BODY);
                int typeIdx = cursor.getColumnIndex(Telephony.Sms.TYPE);

                while (cursor.moveToNext()) {
                    String address = cursor.getString(addrIdx);
                    long   date    = cursor.getLong(dateIdx);
                    String body    = cursor.getString(bodyIdx);
                    int    type    = cursor.getInt(typeIdx);
                    list.add(new SMS(address, date, body, type));
                }
            }
        }
        return list;
    }
}
