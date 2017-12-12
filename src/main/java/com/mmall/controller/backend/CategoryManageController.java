package com.mmall.controller.backend;

import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mmall.common.Constants;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Category;
import com.mmall.pojo.User;
import com.mmall.service.ICategoryService;
import com.mmall.service.IUserService;

@Controller
@RequestMapping("/manage/category")
public class CategoryManageController {
	
	@Autowired
	private ICategoryService categoryService;
	
	private IUserService userService;
	
	@RequestMapping(value = "addCategory", method = RequestMethod.POST)
	@ResponseBody
	public ServerResponse<String> addCategory(HttpSession session, 
			String categoryName, @RequestParam(value = "parentId", defaultValue = "0")int parentId) {
		User user = (User) session.getAttribute(Constants.SESSION_USER_INFO);
		if (user == null) {
			return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录");
		} else if (user.getRole().intValue() != Constants.Role.ROLE_ADMIN) {
			return ServerResponse.createByErrorMessage("当前用户没有该权限");
		}
		
		return categoryService.addCategory(categoryName, parentId);
	}
	
	@RequestMapping(value = "updateCategory")
	@ResponseBody
	public ServerResponse updateCategory(HttpSession session, String categoryName, Integer categoryId) {
		User user = (User) session.getAttribute(Constants.SESSION_USER_INFO);
		if (user == null) {
			return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录");
		} else if (user.getRole().intValue() != Constants.Role.ROLE_ADMIN) {
			return ServerResponse.createByErrorMessage("当前用户没有该权限");
		}
		
		return categoryService.updateCategory(categoryName, categoryId);
	}
	
	@RequestMapping(value = "selectChiledCategory")
	@ResponseBody
	public ServerResponse<List<Category>> selectChildCategorySameLevel(HttpSession session, @RequestParam(value = "categoryId", defaultValue = "0")Integer categoryId) {
		User user = (User) session.getAttribute(Constants.SESSION_USER_INFO);
		if (user == null) {
			return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录");
		} else if (user.getRole().intValue() != Constants.Role.ROLE_ADMIN) {
			return ServerResponse.createByErrorMessage("当前用户没有该权限");
		}
		
		return categoryService.selectChildCategorySameLevel(categoryId);
	}
	
	@RequestMapping(value = "selectDeepChild")
	@ResponseBody
	public ServerResponse selectDeepChild(HttpSession session, @RequestParam(value = "categoryId", defaultValue = "0")Integer categoryId) {
		User user = (User) session.getAttribute(Constants.SESSION_USER_INFO);
		if (user == null) {
			return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录");
		} else if (user.getRole().intValue() != Constants.Role.ROLE_ADMIN) {
			return ServerResponse.createByErrorMessage("当前用户没有该权限");
		}
		
		return categoryService.selectChildDeep(categoryId);
	}
	
}
