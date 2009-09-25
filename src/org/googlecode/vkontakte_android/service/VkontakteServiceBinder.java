package org.googlecode.vkontakte_android.service;

import java.io.IOException;
import java.util.Date;

import org.googlecode.userapi.Credentials;
import org.googlecode.userapi.Message;
import org.googlecode.userapi.VkontakteAPI;
import org.googlecode.vkontakte_android.CSettings;

import android.content.Context;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

public class VkontakteServiceBinder extends IVkontakteService.Stub{

	private static final String TAG = "Service-Iface";
	
	private Context m_context;
	private CheckingService m_service;
	
	VkontakteServiceBinder(CheckingService s) {
		m_service = s;
		m_context = s.getApplicationContext();
	}

	@Override
	public boolean login(String login, String pass) throws RemoteException {
		VkontakteAPI api = ApiCheckingKit.getApi();
		Context ctx = m_context;
		
		try {
			Credentials cred = new Credentials(login, pass, null, null);
		  if (api.login(cred)) {
				CSettings.saveLogin(ctx, login, pass, api.getCred().getRemixpass(), api.getCred().getSession());
				return true; 
		  }
		} catch (IOException e) {
			e.printStackTrace();
			UpdatesNotifier.showError(ctx, "Connection problems");
			return false;
		} 
		return false;
	}

	@Override
	public boolean loginAuth() throws RemoteException {
		Context ctx = m_context;
		VkontakteAPI api = ApiCheckingKit.getApi();
		
		if (CSettings.isLogged(ctx)) {
			try {
				
				//TODO Check this!
				Log.d(TAG, "already logged. using existing log/pass");
				if (!TextUtils.isEmpty(CSettings.getSid(ctx))) {
					Credentials cred = new Credentials(null, null, null, CSettings.getSid(ctx));
					api.login(cred);
					System.out.println("logged with sid");
					return true;
				} else if (api.login( new Credentials(CSettings.getLogin(ctx), CSettings.getPass(ctx), null, null))) {
					// todo: refresh only remix
					CSettings.saveLogin(ctx,
							CSettings.getLogin(ctx), CSettings.getPass(ctx),
							api.getCred().getRemixpass(), api.getCred().getSession());
					return true;
				} else {
					UpdatesNotifier.showError(ctx, "Either login or password is incorrect");
					CSettings.clearPrivateInfo(ctx);
					return false;
				}
			} catch (IOException ex) {
				UpdatesNotifier.showError(ctx, "Connection problems");
				return false;
			}
		} else {
		  Log.d(TAG, "Not authorized");
		  return false;	
		}
	}

	
	@Override
	public void sendMessage(String text, long id) throws RemoteException {
        Message message = new Message();
        message.setDate(new Date());
        message.setReceiverId(id);
        message.setText(text);
		try {
			ApiCheckingKit.getApi().sendMessageToUser(message);
		} catch (IOException e) {
			UpdatesNotifier.showError(m_context,"Cannot send the message. Check connection.");
			e.printStackTrace();
		}
	}

	@Override
	public void sendStatus(String status) throws RemoteException {
	}

	@Override
	public void update(int what) throws RemoteException {
		m_service.doCheck(what);
	}

	
	
	@Override
	public void logout() throws RemoteException {
		try {
			Log.d(TAG, "Logout");
			ApiCheckingKit.getApi().logout();
			CSettings.clearPrivateInfo(m_context);
		} catch (IOException e) {
			UpdatesNotifier.showError(m_context, "Connection problems");
			e.printStackTrace();
		}
	}

	@Override
	public void stop() throws RemoteException {
		m_service.stopSelf();
	}


}
