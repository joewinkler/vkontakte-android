package org.googlecode.vkontakte_android;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

public class CMessageFactory {

	public static View getMessageView(Context ctx, String from, String message) {
		View v = LayoutInflater.from(ctx).inflate(R.layout.messages_record,
				null);

		TextView vfrom = (TextView) v.findViewById(R.id.mess_from);
		vfrom.setText(from);

		TextView vmess = (TextView) v.findViewById(R.id.message_text);
		vmess.setText(message);

		ImageButton b = (ImageButton) v.findViewById(R.id.answer_btn);
        b.setImageDrawable(new BitmapDrawable(CImagesManager.getBitmap("reply")));
        	
		return v;

	}

}
