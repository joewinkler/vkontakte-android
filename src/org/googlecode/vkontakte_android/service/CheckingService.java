package org.googlecode.vkontakte_android.service;

import java.io.IOException;
import java.util.Map;
import java.util.List;
import java.util.LinkedList;
import java.util.Collections;

import org.googlecode.vkontakte_android.service.ApiCheckingKit.UpdateType;
import org.json.JSONException;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

public class CheckingService extends Service {

    private static final int UPDATE_FRIENDS = 1;
    private List<Thread> threads = Collections.synchronizedList(new LinkedList<Thread>());
    //boolean m_hasConnection = true;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onStart(final Intent intent, int startId) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d("serv", "service started");
                switch (intent.getIntExtra("action", -1)) {
                    case UPDATE_FRIENDS:
                        updateFriends();
                        break;
                    default:
                        updateHistory();
                }
            }
        });
        threads.add(t);
        t.start();
    }

    private void updateFriends() {
        //todo: implement
    }

    private void updateHistory() {
        //todo: implement
        try {
            ApiCheckingKit kit = ApiCheckingKit.getInstance();
            Map<UpdateType, Long> res = kit.getUpdates(); //fetch updates from site
            processMessages(kit, res);
            processFriends(kit, res);
            processPhotoTags(kit, res);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (JSONException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }


    private void processMessages(ApiCheckingKit kit, Map<UpdateType, Long> res) {
        long incomingMess = res.get(UpdateType.MESSAGES)
                - kit.getPreviosUnreadMessNum();

        if (incomingMess == 0) //messages count didn't changed since last checking
            return;

        if (incomingMess > 0) // new incoming messages
        {
            UpdatesNotifier.notify(getApplicationContext(), "New messages: " + incomingMess);
            kit.setPreviosUnreadMessNum(res.get(UpdateType.MESSAGES));
        } else // some messages were read by another way
        {
            kit.setPreviosUnreadMessNum(res.get(UpdateType.MESSAGES));
        }
    }

    private void processFriends(ApiCheckingKit kit, Map<UpdateType, Long> res) {
        long incomingFr = res.get(UpdateType.FRIENDSHIP_REQ)
                - kit.getPreviosFriendshipRequestsNum();

        if (incomingFr == 0)
            return;

        if (incomingFr > 0) {
            UpdatesNotifier.notify(getApplicationContext(), "New friends: " + incomingFr);
            kit.setPreviosFriendshipRequestsNum(res.get(UpdateType.FRIENDSHIP_REQ));
        } else {
            kit.setPreviosFriendshipRequestsNum(res.get(UpdateType.MESSAGES));
        }
    }

    private void processPhotoTags(ApiCheckingKit kit, Map<UpdateType, Long> res) {
        long incomingTags = res.get(UpdateType.TAGS)
                - kit.getPreviosNewPhotoTagsNum();

        if (incomingTags == 0)
            return;

        if (incomingTags > 0) {
            UpdatesNotifier.notify(getApplicationContext(), "New photo tags: " + incomingTags);
            kit.setPreviosNewPhotoTagsNum(res.get(UpdateType.TAGS));
        } else {
            kit.setPreviosNewPhotoTagsNum(res.get(UpdateType.TAGS));
        }
    }

    @Override
    public void onDestroy() {
        Log.d("serv", "service stopped");
        //todo: stop all running threads
        super.onDestroy();
    }
}


