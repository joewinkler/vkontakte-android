package org.googlecode.vkontakte_android.service;

interface IVkontakteService {
  void update(int what);
  boolean sendMessage(String mess, long id);
  boolean sendStatus(String status);
  
  boolean login(String login, String pass);
  boolean loginAuth();
  boolean logout();
  
  
  boolean loadPrivateMessages(long userid, int num);
  
  //load user's profile to the cache and return if successful
  boolean loadProfile(long userid);
  
  
  void stop();
   
}