package com.mmall.controller.protl;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mmall.common.Constants;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;

/**
 * 
 * @author lishuangqing
 * @createDate 2017��11��14��
 */

@Controller
@RequestMapping("/user/")
public class UserController {
	
	@Autowired
	private IUserService userService;
	
	/**
	 * �û���¼
	 * @createUser lishuangqing
	 * @createDate 2017��11��14��
	 * 
	 * @updateDate
	 * @updateUser
	 * @updateComment
	 * 
	 * @param userName �û���
	 * @param password ����
	 * @param session session
	 * @return �����û���Ϣ��json��Ϣ
	 */
	@RequestMapping(value = "login", method = RequestMethod.POST)
	@ResponseBody
	public ServerResponse<User> login(String userName, String password, HttpSession session) {
		ServerResponse<User> response = userService.toLogin(userName, password);
		if (response.isSuccess()) {
			session.setAttribute(Constants.SESSION_USER_INFO, response.getData());
		}
		
		return response;
	}
	
	@RequestMapping(value = "login_out", method = RequestMethod.POST)
	@ResponseBody
	public ServerResponse<User> loginOut(HttpSession session) {
		session.removeAttribute(Constants.SESSION_USER_INFO);
		return ServerResponse.createBySuccess();
	}
	
	@RequestMapping(value = "register", method = RequestMethod.POST)
	@ResponseBody
	public ServerResponse<String> register(User user) {
		return userService.register(user);
	}
	
	@RequestMapping(value = "check_valid", method = RequestMethod.POST)
	@ResponseBody
	public ServerResponse<String> checkValid(String str, String type) {
		return userService.checkValid(str, type);
	}
	
	@RequestMapping(value = "get_user_info", method = RequestMethod.POST)
	@ResponseBody
	public ServerResponse<User> getUserInfo(HttpSession session) {
		User user = (User) session.getAttribute(Constants.SESSION_USER_INFO);
		if (user != null) {
			return ServerResponse.createBySuccess(user);
		}
		return ServerResponse.createByErrorMessage("用户未登录");
	}
	
	@RequestMapping(value = "forget_password", method = RequestMethod.POST)
	@ResponseBody
	public ServerResponse<String> forgetPassowrd(String userName) {
		return userService.forgetPassword(userName);
	}
	
	@RequestMapping(value = "valid_answer", method = RequestMethod.POST)
	@ResponseBody
	public ServerResponse<String> validAnswer(String userName, String question, String answer) {
		
		return userService.validAnswer(userName, question, answer);
	}
	
	@RequestMapping(value = "resetPassword", method = RequestMethod.POST)
	@ResponseBody
	public ServerResponse<String> resetPassword(HttpSession session, String password, String newPassword) {
		User user = (User) session.getAttribute(Constants.SESSION_USER_INFO);
		if (user == null) {
			return ServerResponse.createByErrorMessage("用户未登录");
		}
		
		return userService.resetPassword(password, newPassword, user);
	}
	
	@RequestMapping(value = "updateUserInfo", method = RequestMethod.POST)
	@ResponseBody
	public ServerResponse<User> updateUserInfo(HttpSession session, User user) {
		User oldUserInfo = (User) session.getAttribute(Constants.SESSION_USER_INFO);
		user.setId(oldUserInfo.getId());
		
		ServerResponse<User> response = userService.updateUserInfo(user);
		//如果更新成功，修改session中的内容
		if (response.isSuccess()) {
			session.setAttribute(Constants.SESSION_USER_INFO, response.getData());
		}
		return response;
	}
	
	/**
	 * 
	 * @createUser lishuangqing
	 * @createDate 2017年11月18日
	 * 
	 * @updateDate
	 * @updateUser
	 * @updateComment
	 * 
	 * @param session
	 * @return
	 */
	@RequestMapping(value = "getUserInfo", method = RequestMethod.POST)
	@ResponseBody
	public ServerResponse<User> get_UserInfo(HttpSession session) {
		User user = (User) session.getAttribute(Constants.SESSION_USER_INFO);
		if (user == null) {
			return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，需要强制登录");
		}
		
		return userService.getUserInfo(user.getId());
	}
}
