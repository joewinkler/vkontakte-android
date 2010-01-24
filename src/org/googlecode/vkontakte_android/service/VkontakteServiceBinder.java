package org.googlecode.vkontakte_android.service;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import org.googlecode.userapi.*;
import org.googlecode.vkontakte_android.R;
import org.googlecode.vkontakte_android.database.ProfileDao;
import org.googlecode.vkontakte_android.database.UserDao;
import org.googlecode.vkontakte_android.provider.UserapiDatabaseHelper;
import org.googlecode.vkontakte_android.provider.UserapiProvider;
import org.googlecode.vkontakte_android.service.CheckingService.contentToUpdate;
import org.googlecode.vkontakte_android.utils.AppHelper;
import org.googlecode.vkontakte_android.utils.PreferenceHelper;
import org.json.JSONException;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;

public class VkontakteServiceBinder extends IVkontakteService.Stub {

    private static final String TAG = "VK:Service-Iface";

    private Context m_context;
    private CheckingService m_service;

    VkontakteServiceBinder(CheckingService s) {
        m_service = s;
        m_context = s.getApplicationContext();
    }

    @Override
    public void login(String login, String pass, String remix) throws RemoteException {
        VkontakteAPI api = ApiCheckingKit.getApi();
        Context ctx = m_context;
        try {
            Log.d(TAG, "Trying to log with login/pass (or remix)");
            Credentials cred = new Credentials(login, pass, remix);
            api.login(cred);
            Log.d(TAG, "Successful log with login/pass (or remix)");
            PreferenceHelper.saveLogin(ctx, cred);
            if (remix == null) {
                //myId is available only when logging with login/pass but not with remix
                PreferenceHelper.saveMyId(ctx, api.myId);
            }

            //todo: is really required here?
            restartScheduledUpdates(ctx);

        } catch (IOException e) {
            throw new MyRemoteException(e);
//            UpdatesNotifier.showError(ctx, R.string.err_msg_connection_problem);
        } catch (UserapiLoginException e) {
            throw new MyRemoteException(e);
        }
    }

    @Override
    public boolean loginAuth() throws RemoteException {
        Context ctx = m_context;
        VkontakteAPI api = ApiCheckingKit.getApi();
        boolean result = false;
        if (PreferenceHelper.isLogged(ctx)) {
            try {
                Credentials credentials = PreferenceHelper.getCredentials(ctx);
                api.login(credentials);
                result = true;
                Log.d(TAG, "Logged in");
                PreferenceHelper.saveLogin(ctx, credentials);
                PreferenceHelper.saveMyId(m_context, api.myId);
                restartScheduledUpdates(ctx);
            } catch (IOException ex) {
                PreferenceHelper.clearPrivateInfo(ctx);
                UpdatesNotifier.showError(ctx, R.string.err_msg_connection_problem);
            } catch (UserapiLoginException e) {
                PreferenceHelper.clearPrivateInfo(ctx);
                e.printStackTrace();
            }
        } else {
            Log.d(TAG, "No Login/Password stored");
        }
        return result;
    }

    private void restartScheduledUpdates(Context ctx) {
        int period = PreferenceHelper.getSyncPeriod(ctx);
        if (period != PreferenceHelper.SYNC_INTERVAL_NEVER)
            ctx.startService(new Intent(AppHelper.ACTION_SET_AUTOUPDATE).putExtra(AppHelper.EXTRA_AUTOUPDATE_PERIOD, period));
    }

    @Override
    public boolean sendMessage(String text, long id) throws RemoteException {
        Message message = new Message();
        message.setDate(new Date());
        message.setReceiverId(id);
        message.setText(text);
        try {
            try {
                ApiCheckingKit.getApi().sendMessageToUser(message);
            } catch (UserapiLoginException e) {
                e.printStackTrace();
            }
            m_service.doCheck(CheckingService.contentToUpdate.MESSAGES_OUT.ordinal(), new Bundle(), false);
        } catch (IOException e) {
            UpdatesNotifier.showError(m_context,
                    R.string.err_msg_check_connection);
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public boolean sendStatus(String status) throws RemoteException {
        boolean result = false;
        try {
            result = ApiCheckingKit.getApi().setStatus(status);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (UserapiLoginException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public void update(int what, boolean synchronous) throws RemoteException {
        m_service.doCheck(what, new Bundle(), synchronous);
    }

    @Override
    public boolean logout() throws RemoteException {
        try {
            Log.d(TAG, "Logout");
            ApiCheckingKit.getApi().logout();
            PreferenceHelper.clearPrivateInfo(m_context);
        } catch (IOException e) {
            UpdatesNotifier.showError(m_context, R.string.err_msg_connection_problem);
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public void stop() throws RemoteException {
        if(m_service!=null){
    	m_service.stopSelf();
        }else{
        	throw new RemoteException();
        }
    }

    @Override
    public boolean loadPrivateMessages(int type, int first, int last)
            throws RemoteException {
        try {
            switch (contentToUpdate.values()[type]) {
                case MESSAGES_IN:
                    m_service.updateInMessages(first, last);
                    return true;
                case MESSAGES_OUT:
                    m_service.updateOutMessages(first, last);
                    return true;
                default:
                    m_service.updateInMessages(first, last / 2);
                    m_service.updateOutMessages(first, last / 2);
                    return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void loadProfile(long userid) throws RemoteException {
        
    	Log.d(TAG,"Start loading profile:"+ String.valueOf(userid));
    	
    	ProfileInfo pr = null;
        try {
            try {
                pr = ApiCheckingKit.getApi().getProfileOrThrow(userid);
            } catch (UserapiLoginException e) {
                e.printStackTrace();
            }

            ProfileDao dao = null;
            if (pr != null) {
                dao = new ProfileDao(pr.getId(), pr.getFirstname(), pr.getSurname(), (pr.getStatus() == null) ? null : pr.getStatus().getText(),
                        pr.getSex(), pr.getBirthday(), pr.getPhone(), pr.getPoliticalViews(), pr.getFamilyStatus(), pr.getCurrentCity());
            }
            Uri uri = null;
            if (dao != null) {
                uri = dao.saveOrUpdate(m_context);
            }
            if (uri != null) {
                Log.d(TAG, uri.toString());
            }

            String photoUrl = null;
            if (pr != null) {
                photoUrl = pr.getPhoto();
            }
            byte photo[] = null;
            if (photoUrl != null) {
                try {
                    photo = ApiCheckingKit.getApi().getFileFromUrl(photoUrl);
                } catch (Exception e) {
                    Log.e(TAG, "cannot load photo", e);
                }
            }
            
            if (photo!=null){
            OutputStream os = m_context.getContentResolver().openOutputStream(uri);
            os.write(photo);
            os.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (pr != null) {
            Log.d(TAG,"Finish loading:"+ pr.toString());
        }else{
        	throw new RemoteException();
        }
        	
        
    }

    @Override
    public void loadMyProfile() throws RemoteException {
        Log.d(TAG, "" + ApiCheckingKit.getApi().myId);
        loadProfile(ApiCheckingKit.getApi().myId);
    }

    @Override
    public void loadStatuses(int start, int end) throws RemoteException {
        try {
            m_service.updateStatuses(start, end);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RemoteException();
        } catch (JSONException e) {
            e.printStackTrace();
            throw new RemoteException();
        }
    }

    public void loadStatusesByUser(int start, int end, long userId) throws RemoteException {
        try {
            m_service.updateStatusesForUser(start, end, userId);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RemoteException();
        } catch (JSONException e) {
            e.printStackTrace();
            throw new RemoteException();
        }
    }

    /**
     * Load photos for users with given ids
     */
    @Override
    public synchronized boolean loadUsersPhotos(List<String> l) throws RemoteException {
        StringBuffer users = new StringBuffer(" ");
        for (String ids : l) {
            users.append(ids).append(",");
        }
        users.deleteCharAt(users.length() - 1);//remove last ','

        Log.d(TAG, "Ids to update:" + users);
        Cursor c = m_context.getContentResolver().query(UserapiProvider.USERS_URI, null,
                UserapiDatabaseHelper.KEY_USER_USERID + " IN(" + users + ")", null, null);
        while (c.moveToNext()) {
            UserDao ud = new UserDao(c);
            try {
                if (ud._data == null) {   ////!!!
                    ud.updatePhoto(m_context);
                }
            } catch (IOException e) {
                Log.e(TAG, "Cannot download photo");
                e.printStackTrace();
            }
        }
        c.close();
        return false;
    }

    @Override
    public boolean loadAllUsersPhotos() throws RemoteException {
        Cursor c = m_context.getContentResolver().query(UserapiProvider.USERS_URI, null,
                null, null, null);
        while (c.moveToNext()) {
            UserDao ud = new UserDao(c);
            try {
                if (ud._data == null) {
                    ud.updatePhoto(m_context);
                }
            } catch (IOException e) {
                Log.e(TAG, "Cannot download photo");
                e.printStackTrace();
            }
        }
        c.close();
        return false;
    }
}
