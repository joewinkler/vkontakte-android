/**
 *
 *
 * @author Ayzen
 */
package org.googlecode.vkontakte_android.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;
import org.googlecode.vkontakte_android.CImagesManager;
import org.googlecode.vkontakte_android.service.ApiCheckingKit;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 *
 * @author Ayzen
 */
public class AvatarLoader {

    private static final String TAG = "VK:AvatarLoader";

    private static final int FETCH_AVATAR_MSG = 1;

    private static HashMap<String, Bitmap> bitmapCache = new HashMap<String, Bitmap>();
    private List<ImageView> missedAvatars = new ArrayList<ImageView>();

    private ExecutorService threadPool;
    private AvatarFetchHandler avatarFetchHandler;

    private Context context;

    public AvatarLoader(Context context) {
        this.context = context;
        avatarFetchHandler = new AvatarFetchHandler();
    }

    public void setAvatarNow(ImageView view) {
        setAvatar(view, true);
    }

    public void setAvatarDeferred(ImageView view) {
        setAvatar(view, false);
    }

    private void setAvatar(ImageView view, boolean loadNow) {
        Bitmap avatar;
        String avatarUrl = (String) view.getTag();

        Log.v(TAG, "Request for avatar: " + avatarUrl);

        // Look for the cached bitmap
        avatar = bitmapCache.get(avatarUrl);

        //Bind the avatar or use default image instead
        if (avatar != null) {
            Log.v(TAG, "Using avatar from bitmap cache");
            view.setImageBitmap(avatar);
        }
        else {
            // Cache miss
            // Set default image
            view.setImageBitmap(CImagesManager.getBitmap(context, CImagesManager.Icons.STUB));
            // That avatar should be loaded
            missedAvatars.add(view);

            if (loadNow)
                loadAvatar(view);
        }
    }

    public void loadMissedAvatars() {
        int missedItems = missedAvatars.size();
        if (missedItems > 0) {
            Log.v(TAG, "Starting to load missed avatars: " + missedItems);
            for (ImageView view : missedAvatars)
                loadAvatar(view);
        }
    }

    public synchronized void cancelDeferredLoading() {
        if (threadPool != null) {
            Log.v(TAG, "Canceling all deferred avatar loadings");
            threadPool.shutdownNow();
            threadPool = null;
        }
        avatarFetchHandler.clearAvatarFetching();
    }

    private void loadAvatar(ImageView view) {
        if (threadPool == null)
            threadPool = Executors.newFixedThreadPool(3);

        threadPool.execute(new AvatarFetcher(view));
    }

    private Bitmap downloadAvatar(String avatarUrl) {
        Log.d(TAG, "Downloading avatar: " + avatarUrl);

        FileOutputStream out = null;
        try {
            String avatarFileName = avatarUrl.substring(avatarUrl.lastIndexOf("/") + 1);
            byte[] avatar = ApiCheckingKit.getApi().getFileFromUrl(avatarUrl);

            if (avatar != null) {
                try {
                    out = new FileOutputStream(AppHelper.AVATARS_DIR + avatarFileName);
                    out.write(avatar);
                    out.close();
                }
                finally {
                    if (out != null)
                        out.close();
                }

                return BitmapFactory.decodeByteArray(avatar, 0, avatar.length);
            }
        }
        catch (IOException e) {
            Log.e(TAG, "Error while downloading avatar: " + avatarUrl, e);
        }

        return null;
    }

    private class AvatarFetchHandler extends Handler {

        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case FETCH_AVATAR_MSG: {
                    final ImageView view = (ImageView) message.obj;
                    String avatarUrl = (String) view.getTag();

                    Bitmap avatar = bitmapCache.get(avatarUrl);

                    if (avatar != null) {
                        Log.v(TAG, "Applying avatar to view: " + avatarUrl);

                        view.setImageBitmap(avatar);
                        missedAvatars.remove(view);
                    }
                }
            }
        }

        public void clearAvatarFetching() {
            removeMessages(FETCH_AVATAR_MSG);
        }

    }

    private class AvatarFetcher implements Runnable {

        private ImageView view;

        public AvatarFetcher(ImageView imageView) {
            this.view = imageView;
        }

        public void run() {
            String avatarUrl = (String) view.getTag();

            Log.v(TAG, "Fetching avatar: " + avatarUrl);

            if (Thread.interrupted())
                return; // shutdown has been called.

            Bitmap avatar = null;
            try {
                // Trying to load avatar from file system
                String avatarFileName = avatarUrl.substring(avatarUrl.lastIndexOf("/") + 1);
                avatar = BitmapFactory.decodeFile(AppHelper.AVATARS_DIR + avatarFileName);

                // If avatar is not saved on device
                if (avatar == null) {
                    if (Thread.interrupted())
                        return; // shutdown has been called.

                    avatar = downloadAvatar(avatarUrl);
                }
            }
            catch (OutOfMemoryError e) {
                // Not enough memory for the avatar, do nothing.
            }

            if (avatar == null) {
                return;
            }

            bitmapCache.put(avatarUrl, avatar);

            if (Thread.interrupted())
                return; // shutdown has been called.

            // Update must happen on UI thread
            Message msg = new Message();
            msg.what = FETCH_AVATAR_MSG;
            msg.obj = view;
            avatarFetchHandler.sendMessage(msg);
        }

    }

}
