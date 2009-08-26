package org.googlecode.vkontakte_android;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.ImageView;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas.VertexMode;

import org.googlecode.userapi.Message;
import org.googlecode.userapi.User;
import org.googlecode.userapi.VkontakteAPI;
import org.googlecode.vkontakte_android.R;
import org.json.JSONException;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class MessagesListAdapter extends BaseAdapter {

    private List<Message> messages = new LinkedList<Message>();
    private Context context;
    private int layout;  //???
    private LayoutInflater layoutInflater;
    //private boolean loading = false;

    public int getCount() {
        return messages.size();
    }

    public Object getItem(int pos) {
        return (Object) pos;
    }

    public long getItemId(int pos) {
        return pos;
    }

    public MessagesListAdapter(Context context, int layout) {
        this.context = context;
        this.layout = layout;
        layoutInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        VkontakteAPI api = CGuiTest.api;
        try {
            messages = api.getPrivateMessages(api.id, 0, 5, VkontakteAPI.privateMessagesTypes.inbox);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public View getView(int pos, View v, ViewGroup p) {
  
    	Message mess = messages.get(pos); 
    	Bitmap bm = null;
    	try {
            byte[] photoByteArray = mess.getSender().getUserPhotoSmall();
            bm = BitmapFactory.decodeByteArray(photoByteArray, 0, photoByteArray.length);
        } catch (IOException e) {
            e.printStackTrace();
        }
    	
        return CMessageFactory.getMessageView(context, mess.getSender().getUserName(), bm, mess.getText());
        
    }


    public void prepareData() {
//        this.count += 1;
//        loading = true;
//        this.notifyDataSetChanged();
    }
}