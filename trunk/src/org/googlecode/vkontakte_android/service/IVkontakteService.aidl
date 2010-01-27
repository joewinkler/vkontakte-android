package org.googlecode.vkontakte_android.service;

interface IVkontakteService {
  void update(int what, boolean synchronous);
  
  boolean sendMessage(String mess, long id);
  boolean sendStatus(String status);
  
  void login(String login, String pass, String remix);
//  void login(Credentials credentials);
  boolean loginAuth();
  boolean logout();
  
  
  boolean loadPrivateMessages(int type, int first, int last);
  void loadStatuses(int start, int end);
  void loadStatusesByUser(int start, int end, long id);
  
  boolean loadUsersPhotos(in List<String> l);  //how to pass Long as type?
  boolean loadAllUsersPhotos(); 
  
  //load user's profile to the cache and return if successful
  void loadProfile(long userid);
  void loadMyProfile();
}