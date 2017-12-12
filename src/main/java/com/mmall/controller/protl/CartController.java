package com.mmall.controller.protl;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mmall.common.Constants;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.ICartService;
import com.mmall.vo.CartVo;

/**
 * 
 * @author lishuangqing
 * @createDate 2017年12月1日
 */
@Controller
@RequestMapping("/cart/")
public class CartController {
	@Autowired
	private ICartService cartService;
	
	@RequestMapping(value = "list")
	@ResponseBody
	public ServerResponse<CartVo> list(HttpSession session) {
		User user = (User)session.getAttribute(Constants.SESSION_USER_INFO);
        if(user ==null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return cartService.list(user.getId());
	}
	
	@RequestMapping(value = "add")
	@ResponseBody
	public ServerResponse<CartVo> add(HttpSession session, Integer count, Integer productId) {
		User user = (User)session.getAttribute(Constants.SESSION_USER_INFO);
        if(user ==null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return cartService.addProductToCart(user.getId(), productId, count);
	}
	
	@RequestMapping(value = "update")
	@ResponseBody
	public ServerResponse<CartVo> update(HttpSession session, Integer count, Integer productId) {
		User user = (User)session.getAttribute(Constants.SESSION_USER_INFO);
        if(user ==null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return cartService.updateProductToCart(user.getId(), productId, count);
	}
	
	@RequestMapping(value = "delete")
	@ResponseBody
	public ServerResponse<CartVo> delete(HttpSession session, String productIds) {
		User user = (User)session.getAttribute(Constants.SESSION_USER_INFO);
        if(user ==null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return cartService.delete(user.getId(), productIds);
	}
}
