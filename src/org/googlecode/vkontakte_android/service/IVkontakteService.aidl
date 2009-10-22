package org.googlecode.vkontakte_android.service;

interface IVkontakteService {
  void update(int what);
  boolean sendMessage(String mess, long id);
  boolean sendStatus(String status);
  
  boolean login(String login, String pass);
  boolean loginAuth();
  boolean logout();
  
  
  boolean loadPrivateMessages(int type, int first, int last);
  boolean loadStatuses(int start, int end);
  
  //load user's profile to the cache and return if successful
  boolean loadProfile(long userid,boolean setMe);
  boolean loadMyProfile();

  
  
  void stop();
   
}