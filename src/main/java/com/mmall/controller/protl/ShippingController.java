package com.mmall.controller.protl;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.github.pagehelper.PageInfo;
import com.mmall.common.Constants;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Shipping;
import com.mmall.pojo.User;
import com.mmall.service.IShippingService;

/**
 * 地址管理
 * @author lishuangqing
 * @createDate 2017年12月2日
 */
@Controller
@RequestMapping("/shipping/")
public class ShippingController {
	@Autowired
	private IShippingService shippingService;
	
	@RequestMapping(value = "add")
	@ResponseBody
	public ServerResponse addShipping(HttpSession session, Shipping shipping) {
		User user = (User) session.getAttribute(Constants.SESSION_USER_INFO);
		if (user == null) {
			return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
		}
		return shippingService.addShipping(user.getId(), shipping);
	}
	
	@RequestMapping(value = "delete")
	@ResponseBody
	public ServerResponse deleteShipping(HttpSession session, Integer shippingId) {
		User user = (User) session.getAttribute(Constants.SESSION_USER_INFO);
		if (user == null) {
			return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
		}
		return shippingService.deleteShipping(user.getId(), shippingId);
	}
	
	@RequestMapping(value = "update")
	@ResponseBody
	public ServerResponse updateShipping(HttpSession session, Shipping shipping) {
		User user = (User) session.getAttribute(Constants.SESSION_USER_INFO);
		if (user == null) {
			return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
		}
		return shippingService.updateShipping(user.getId(), shipping);
	}
	
	@RequestMapping(value = "select")
	@ResponseBody
	public ServerResponse selectShipping(HttpSession session, Integer shippingId) {
		User user = (User) session.getAttribute(Constants.SESSION_USER_INFO);
		if (user == null) {
			return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
		}
		return shippingService.selectShipping(user.getId(), shippingId);
	}
	
	@RequestMapping(value = "list")
	@ResponseBody
	public ServerResponse<PageInfo> list(HttpSession session, 
			@RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
			@RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
		User user = (User) session.getAttribute(Constants.SESSION_USER_INFO);
		if (user == null) {
			return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), ResponseCode.NEED_LOGIN.getDesc());
		}
		return shippingService.selectListOfShipping(user.getId(), pageNum, pageSize);
	}
}
