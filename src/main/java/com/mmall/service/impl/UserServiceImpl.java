package com.mmall.service.impl;

import java.util.UUID;

import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mmall.common.Constants;
import com.mmall.common.ServerResponse;
import com.mmall.common.TokenCache;
import com.mmall.dao.UserMapper;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import com.mmall.util.MD5Util;

@Service
public class UserServiceImpl implements IUserService {
	
	@Autowired
	private UserMapper userMapper;
	
	@Override
	public ServerResponse<User> toLogin(String userName, String password) {
		int count = userMapper.checkUserName(userName);
		if (count == 0) {
			return ServerResponse.createByErrorMessage("用户名不存在");
		}
		
		String md5Password = MD5Util.MD5EncodeUtf8(password);
		
		User user = userMapper.selectLogin(userName, md5Password);
		if (user == null) {
			return ServerResponse.createByErrorMessage("密码错误");
		}
		
		user.setPassword(StringUtils.EMPTY);
		
		return ServerResponse.createBySuccess("登录成功", user);
	}

	@Override
	public ServerResponse<String> register(User user) {
		ServerResponse<String> validate = this.checkValid(user.getUsername(), Constants.USER_NAME);
		if (!validate.isSuccess()) {
			return validate;
		}
		validate = this.checkValid(user.getEmail(), Constants.USER_EMAIL);
		if (!validate.isSuccess()) {
			return validate;
		}
		
		user.setRole(Constants.Role.ROLE_CUSTOMER);
		user.setPassword(MD5Util.MD5EncodeUtf8(user.getPassword()));
		
		int count = userMapper.insert(user);
		if (count == 0) {
			return ServerResponse.createByErrorMessage("注册失败");
		}
		
		return ServerResponse.createBySuccess("注册成功");
	}

	@Override
	public ServerResponse<String> checkValid(String str, String type) {
		
		if (StringUtils.isNotBlank(type)) {
			int resultCount;
			if (Constants.USER_NAME.equals(type)) {
				resultCount = userMapper.checkUserName(str);
				if (resultCount > 0) {
					return ServerResponse.createByErrorMessage("用户名已存在");
				}
			}
			if (Constants.USER_EMAIL.equals(type)) {
				resultCount = userMapper.checkEmail(str);
				if (resultCount > 0) {
					return ServerResponse.createByErrorMessage("email已存在");
				}
			}
			
		} else {
			return ServerResponse.createByErrorMessage("参数错误");
		}
		
		return ServerResponse.createBySuccessMessage("校验成功");
	}

	@Override
	public ServerResponse<String> forgetPassword(String userName) {
		ServerResponse<String> validateUserName = checkValid(userName, Constants.USER_NAME);
		if (validateUserName.isSuccess()) {
			
			return ServerResponse.createByErrorMessage("用户不存在");
		}
		
		String question = userMapper.queryQuestionByUserName(userName);
		if (StringUtils.isNotBlank(question)) {
			return ServerResponse.createBySuccess(question);
		}
		
		return ServerResponse.createByErrorMessage("找回密码的问题是空的");
	}

	@Override
	public ServerResponse<String> validAnswer(String userName, String question, String answer) {
		int count = userMapper.validAnswer(userName, question, answer);
		if (count > 0) {
//			return ServerResponse.createBySuccessMessage("问题的答案正确");
			
			//设计token值
			String forgetToken = UUID.randomUUID().toString();
			
			TokenCache.setKey("token_" + userName, forgetToken);
			
			return ServerResponse.createBySuccess(forgetToken);
			
		}
		return ServerResponse.createByErrorMessage("问题的答案错误");
	}

	@Override
	public ServerResponse<String> resetPassword(String oldPassword, String newPassword, User user) {
		int resultCount = userMapper.checkPassword(MD5Util.MD5EncodeUtf8(oldPassword), user.getId());
		if (resultCount == 0) {
			return ServerResponse.createByErrorMessage("用户密码错误");
		}
		user.setPassword(MD5Util.MD5EncodeUtf8(newPassword));
		
		int updateCount = userMapper.updateByPrimaryKeySelective(user);
		if (updateCount > 0) {
			ServerResponse.createBySuccessMessage("密码修改成功");
		}
		
		return ServerResponse.createBySuccessMessage("密码修改失败");
	}

	@Override
	public ServerResponse<User> updateUserInfo(User user) {
		//用户名不能被更改
		//需要校验邮箱
		int resultCount = userMapper.checkEmailByUserId(user.getEmail(), user.getId());
		//如果查询到，说明邮箱是别人的
		if (resultCount > 0) {
			return ServerResponse.createByErrorMessage("邮箱已存在，请更换");
		}
		
		User updateUser = new User();
		updateUser.setId(user.getId());
		updateUser.setEmail(user.getEmail());
		updateUser.setPhone(user.getPhone());
		updateUser.setQuestion(user.getQuestion());
		updateUser.setAnswer(user.getAnswer());
		
		int updateCount = userMapper.updateByPrimaryKeySelective(updateUser);
		if (updateCount > 0) {
			return ServerResponse.createBySuccess("修改成功", updateUser);
		}
		
		return ServerResponse.createByErrorMessage("修改个人信息失败");
	}

	@Override
	public ServerResponse<User> getUserInfo(int userId) {
		User user = userMapper.selectByPrimaryKey(userId);
		if (user != null) {
			return ServerResponse.createBySuccess("获取成功", user);
		}
		return ServerResponse.createByErrorMessage("获取失败");
	}

	@Override
	public boolean hasLogin(HttpSession session) {
		User user = (User) session.getAttribute(Constants.SESSION_USER_INFO);
		if (user != null) {
			return true;
		}
		return false;
	}
	
	
}
