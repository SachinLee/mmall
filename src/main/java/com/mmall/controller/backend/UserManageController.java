package com.mmall.controller.backend;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mmall.common.Constants;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;

@Controller
@RequestMapping("/manage/user")
public class UserManageController {
	@Autowired
	private IUserService userService;
	
	@RequestMapping(value = "login", method = RequestMethod.POST)
	@ResponseBody
	public ServerResponse<User> login(String userName, String password, HttpSession session) {
		ServerResponse<User> response = userService.toLogin(userName, password);
		if (response.isSuccess()) {
			User user = response.getData();
			if (user.getRole() == Constants.Role.ROLE_ADMIN) {
				session.setAttribute(Constants.SESSION_USER_INFO, user);
			} else {
				return ServerResponse.createByErrorMessage("该用户不是管理员");
			}
			
		}
 		
		return response;
	}
}
