package org.googlecode.vkontakte_android.service;

interface IVkontakteService {
  void update(int what);
  void sendMessage(String mess, long id);
  void sendStatus(String status);
  
  boolean login(String login, String pass);
  boolean loginAuth();
  void logout();
  
  void stop();
   
}