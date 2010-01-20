package org.googlecode.vkontakte_android.service;

interface IVkontakteService {
  void update(int what, boolean synchronous);
  
  boolean sendMessage(String mess, long id);
  boolean sendStatus(String status);
  
  boolean login(String login, String pass, String remix);
//  LoginResult login(Credentials credentials);
  boolean loginAuth();
  boolean logout();
  
  
  boolean loadPrivateMessages(int type, int first, int last);
  boolean loadStatuses(int start, int end);
  boolean loadStatusesByUser(int start, int end, long id);
  
  boolean loadUsersPhotos(in List<String> l);  //how to pass Long as type?
  boolean loadAllUsersPhotos(); 
  
  //load user's profile to the cache and return if successful
  boolean loadProfile(long userid,boolean setMe);
  boolean loadMyProfile();

  
  
  void stop();
  void restartScheduledUpdates();     
    
   
}