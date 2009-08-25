package org.googlecode.vkontakte_android;

import android.app.Activity;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TableLayout;

public class CMeTab extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
        setContentView(R.layout.main);
               
        ImageButton b = (ImageButton) findViewById(R.id.ImageButton01);
        b.setImageDrawable(new BitmapDrawable(CImagesManager.getBitmap("ok")));
        
        TableLayout table = (TableLayout) findViewById(R.id.Wall);
        
        
        //test
        View v1 = CWallMessageFactory.getMessageView(this, "Friendname", "message message message message message message message message message message message message message ");
        View v2 = CWallMessageFactory.getMessageView(this, "Friendname", "message message message message message message message message message message message message message ");
        View v3 = CWallMessageFactory.getMessageView(this, "Friendname","message message message message message message message message message message message message message");
        View v4 = CWallMessageFactory.getMessageView(this, "Friendname", "message message message message message message message message message message message message message");
               
        table.addView(v1);
        table.addView(v2);
        table.addView(v3);
        table.addView(v4);
		
	}

}
