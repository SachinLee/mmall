package com.mmall.controller.backend;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mmall.common.Constants;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IOrderService;
import com.mmall.service.IUserService;

@Controller
@RequestMapping("/manage/order")
public class OrderMangeController {
	
	@Autowired
	private IUserService userService;
	@Autowired
	private IOrderService orderService;
	
	@RequestMapping(value="list")
	@ResponseBody
	public ServerResponse list(HttpSession session,
			@RequestParam(value="pageNum", defaultValue="1") int pageNum,
			@RequestParam(value="pageSize", defaultValue="10") int pageSize) {
		User user = (User) session.getAttribute(Constants.SESSION_USER_INFO);
		if (user == null) {
			return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "请登录");
		}
		if (user.getRole() != Constants.Role.ROLE_ADMIN) {
			return ServerResponse.createByErrorMessage("没有管理员权限");
		}
		
		return orderService.manageList(pageNum, pageSize);
	}
	
	@RequestMapping(value="detail")
	@ResponseBody
	public ServerResponse orderDetail(HttpSession session, Long orderNo) {
		User user = (User) session.getAttribute(Constants.SESSION_USER_INFO);
		if (user == null) {
			return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "请登录");
		}
		if (user.getRole() != Constants.Role.ROLE_ADMIN) {
			return ServerResponse.createByErrorMessage("没有管理员权限");
		}
		
		return orderService.manageDetail(orderNo);
	}
	
	@RequestMapping(value="search")
	@ResponseBody
	public ServerResponse ordersearch(HttpSession session, Long orderNo,
			@RequestParam(value="pageNum", defaultValue="1") int pageNum,
			@RequestParam(value="pageSize", defaultValue="10") int pageSize) {
		User user = (User) session.getAttribute(Constants.SESSION_USER_INFO);
		if (user == null) {
			return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "请登录");
		}
		if (user.getRole() != Constants.Role.ROLE_ADMIN) {
			return ServerResponse.createByErrorMessage("没有管理员权限");
		}
		
		return orderService.manageSearch(orderNo, pageNum, pageSize);
	}
	
	@RequestMapping(value="send_goods")
	@ResponseBody
	public ServerResponse<String> sendGoods(HttpSession session, Long orderNo) {
		User user = (User) session.getAttribute(Constants.SESSION_USER_INFO);
		if (user == null) {
			return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "请登录");
		}
		if (user.getRole() != Constants.Role.ROLE_ADMIN) {
			return ServerResponse.createByErrorMessage("没有管理员权限");
		}
		
		return orderService.sendGoods(orderNo);
	}
}
