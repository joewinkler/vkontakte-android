/**
 *
 *
 * @author Ayzen
 */
package org.googlecode.vkontakte_android.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;
import org.googlecode.vkontakte_android.CImagesManager;
import org.googlecode.vkontakte_android.service.ApiCheckingKit;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This class helps to populate UI (ImageView) with avatars, it responsible for avatars background loading
 * and caching.
 *
 * @author Ayzen
 */
public class AvatarLoader {

    private static final String TAG = "VK:AvatarLoader";

    private static final int FETCH_AVATAR_MSG = 1;

    private static HashMap<String, Bitmap> bitmapCache = new HashMap<String, Bitmap>();
    /**
     * Stack of avatars that should be loaded from remote server.
     */
    private Stack<AvatarInfo> missedAvatars = new Stack<AvatarInfo>();

    /**
     * Thread pool for loading avatars from device.
     */
    private ExecutorService threadPool = null;
    /**
     * Is used for downloading avatars from remote server.
     */
    private Thread avatarLoadThread = null;
    /**
     * Handler for applying avatars in UI thread.
     */
    private AvatarFetchHandler avatarFetchHandler;
    private boolean shouldLoadNext = false;

    private Context context;

    public AvatarLoader(Context context) {
        this.context = context;
        avatarFetchHandler = new AvatarFetchHandler();
    }

    /**
     * Removes cached avatar from device.
     *
     * @param avatarUrl avatar URL
     */
    public static void removeCachedAvatar(String avatarUrl) {
        String avatarFileName = avatarUrl.substring(avatarUrl.lastIndexOf("/") + 1);
        File avatar = new File(AppHelper.AVATARS_DIR + avatarFileName);
        if (avatar.delete())
            Log.d(TAG, "Removed cached avatar: " + avatarUrl);
    }

    /**
     * Applies avatar bitmap to corresponding ImageView.
     *
     * @param info information about avatar
     */
    public void setAvatar(AvatarInfo info) {
        Bitmap avatar;
        ImageView view = info.view;
        String avatarUrl = info.avatarUrl;

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

            loadAvatar(info, false);
        }
    }

    /**
     * Starts to load missed avatars from remote server.
     */
    public void loadMissedAvatars() {
        int missedItems = missedAvatars.size();
        synchronized (AvatarLoader.this) {
            shouldLoadNext = true;
            if (avatarLoadThread == null && missedItems > 0) {
                Log.d(TAG, "Starting to load missed avatars: " + missedItems);
                loadAvatar(missedAvatars.pop(), true);
            }
        }
    }

    /**
     * Cancel avatar remote loading and loading from device.
     */
    public void cancelLoading() {
        Log.d(TAG, "Canceling all load threads");
        if (avatarLoadThread != null) {
            avatarLoadThread.interrupt();
        }
        if (threadPool != null) {
            threadPool.shutdownNow();
            threadPool = null;
        }
    }

    /**
     * Stops avatar loading and clears all deferred loadings.
     */
    public void abortProcess() {
        Log.d(TAG, "Abort avatar loading process");
        cancelLoading();
        missedAvatars.clear();
    }

    /**
     * Starts a thread to load avatar.
     *
     * @param info information about avatar
     * @param loadNow true, if avatar should be loaded instantly
     */
    private void loadAvatar(AvatarInfo info, boolean loadNow) {
        AvatarFetcher fetcher = new AvatarFetcher(info, loadNow);
        if (loadNow) {
            avatarLoadThread = new Thread(fetcher);
            avatarLoadThread.start();
        }
        else {
            if (threadPool == null)
                threadPool = Executors.newSingleThreadExecutor();
            threadPool.execute(fetcher);
        }
    }

    /**
     * Downloading avatar from remote server.
     *
     * @param avatarUrl avatar URL
     * @return bitmap with avatar
     */
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
        catch (InterruptedIOException e) {
            Log.d(TAG, "Avatar download process was aborted: " + avatarUrl);
        }
        catch (IOException e) {
            Log.e(TAG, "Error while downloading avatar: " + avatarUrl, e);
        }

        return null;
    }

    /**
     * Handler for applying bitmaps with avatars to corresponding ImageViews in UI thread.
     */
    private class AvatarFetchHandler extends Handler {

        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case FETCH_AVATAR_MSG: {
                    AvatarInfo info = (AvatarInfo) message.obj;
                    ImageView view = info.view;
                    String url = info.avatarUrl;
                    Bitmap avatar = info.bitmap;

                    // Here ImageView could represent different avatar (if user scrolled from that avatar)
                    if (avatar != null && url.equals(view.getTag()))
                        view.setImageBitmap(avatar);
                }
            }
        }
    }

    /**
     * Loads avatar from device if it's cached, otherwise downloads avatar from remote server.
     */
    private class AvatarFetcher implements Runnable {

        private AvatarInfo info;
        private boolean loadNow;

        public AvatarFetcher(AvatarInfo info, boolean loadNow) {
            this.info = info;
            this.loadNow = loadNow;
        }

        public void run() {
            ImageView view = info.view;
            String avatarUrl = info.avatarUrl;

            Bitmap avatar = null;
            try {
                // Trying to load avatar from file system
                String avatarFileName = avatarUrl.substring(avatarUrl.lastIndexOf("/") + 1);
                avatar = BitmapFactory.decodeFile(AppHelper.AVATARS_DIR + avatarFileName);

                // If avatar is not saved on device
                if (avatar == null) {
                    if (loadNow) {
                        interruptAndTryLoadNext(false);

                        avatar = downloadAvatar(avatarUrl);
                        if (avatar != null) {
                            // View for avatar could be changed
                            int index = missedAvatars.indexOf(info);
                            if (index != -1) {
                                info = missedAvatars.elementAt(index);
                                view = info.view;
                                missedAvatars.remove(info);
                            }
                        }
                        else
                            missedAvatars.push(info);
                    }
                    else {
                        // Should load avatars later
                        // Ensure that there will be only one such avatar in stack
                        missedAvatars.remove(info);
                        missedAvatars.push(info);
                        Log.v(TAG, "Added avatar to pending load: " + avatarUrl);
                    }
                }
            }
            catch (OutOfMemoryError e) {
                // Not enough memory for the avatar, do nothing.
            }

            if (avatar != null) {
                bitmapCache.put(avatarUrl, avatar);
                info.bitmap = avatar;

                interruptAndTryLoadNext(false);

                // Here ImageView could represent different avatar (if user scrolled from that avatar)
                if (avatar != null && avatarUrl.equals(view.getTag())) {
                    // Update must happen on UI thread
                    Message msg = new Message();
                    msg.what = FETCH_AVATAR_MSG;
                    msg.obj = info;
                    avatarFetchHandler.sendMessage(msg);
                }
            }

            interruptAndTryLoadNext(true);
        }

        private void interruptAndTryLoadNext(boolean finished) {
            synchronized (AvatarLoader.this) {
                // If this threat is about to finish
                if (avatarLoadThread == Thread.currentThread() && (Thread.interrupted() || finished)) {
                    avatarLoadThread = null;

                    // If avatar loading was not finished, finish it next time
                    if (Thread.interrupted())
                        missedAvatars.push(info);

                    // If there are not loaded avatars, and they should be loaded now...
                    if (missedAvatars.size() > 0 &&
                        (!Thread.interrupted() || (Thread.interrupted() && shouldLoadNext))) {
                        // Load next avatar from stack
                        loadAvatar(missedAvatars.pop(), true);
                    }
                    shouldLoadNext = false;
                }
            }
        }

    }

    /**
     * Information about avatar and it's corresponding ImageView.
     */
    public static class AvatarInfo {
        
        public ImageView view;
        public String avatarUrl;
        public Bitmap bitmap;

        public AvatarInfo() {
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null || !(obj instanceof AvatarInfo))
                return false;

            return avatarUrl.equals(((AvatarInfo) obj).avatarUrl);
        }
    }

}
