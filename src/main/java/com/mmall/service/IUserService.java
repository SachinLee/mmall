package com.mmall.service;

import javax.servlet.http.HttpSession;

import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;

public interface IUserService {
	
	ServerResponse<User> toLogin(String userName, String password);
	
	ServerResponse<String> register(User user);
	
	ServerResponse<String> checkValid(String str, String type);
	
	ServerResponse<String> forgetPassword(String userName);
	
	ServerResponse<String> validAnswer(String userName, String question, String answer);

	ServerResponse<String> resetPassword(String oldPassword, String newPassword, User user);

	ServerResponse<User> updateUserInfo(User user);
	
	ServerResponse<User> getUserInfo(int userId);
	
	boolean hasLogin(HttpSession session);
}
