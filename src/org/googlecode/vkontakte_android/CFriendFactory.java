package org.googlecode.vkontakte_android;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

public class CFriendFactory {

	public static View getFriendView(Context ctx, String friendname,
			boolean online) {
		View v = LayoutInflater.from(ctx).inflate(R.layout.friend_view, null);

		TextView vfrom = (TextView) v.findViewById(R.id.friend_name);
		vfrom.setText(friendname);

		TextView vmess = (TextView) v.findViewById(R.id.friend_status);
		vmess.setText(online ? "Online" : "Offline");

		ImageButton b = (ImageButton) v.findViewById(R.id.send_message_friend);
		b.setImageDrawable(new BitmapDrawable(CImagesManager.getBitmap("send")));

		return v;

	}

}
