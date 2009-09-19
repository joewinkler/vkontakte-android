package org.googlecode.vkontakte_android.service;

interface IVkontakteService {
  void update(int what);
  void sendMessage(String mess, long id);
  void sendStatus(String status);
}