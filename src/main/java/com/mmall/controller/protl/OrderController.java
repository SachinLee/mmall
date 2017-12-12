package com.mmall.controller.protl;

import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.demo.trade.config.Configs;
import com.google.common.collect.Maps;
import com.mmall.common.Constants;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IOrderService;
import com.mmall.vo.OrderVo;

@Controller
@RequestMapping("/order/")
public class OrderController {
	
	private static final Logger logger = LoggerFactory.getLogger(OrderController.class);
	
	@Autowired
	private IOrderService orderService;
	
	@RequestMapping(value = "create")
	@ResponseBody
	public ServerResponse createOrder(HttpSession session, Integer shippingId) {
		User user = (User)session.getAttribute(Constants.SESSION_USER_INFO);
        if(user ==null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        
		return orderService.createOrder(user.getId(), shippingId);
	}
	
	@RequestMapping(value = "cancle")
	@ResponseBody
	public ServerResponse cancleOrder(HttpSession session, Long orderNo) {
		User user = (User)session.getAttribute(Constants.SESSION_USER_INFO);
        if(user ==null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        
		return orderService.cancleOrder(user.getId(), orderNo);
	}
	
	@RequestMapping(value = "get_order_cart_product")
	@ResponseBody
	public ServerResponse getOrderCartProduct(HttpSession session) {
		User user = (User)session.getAttribute(Constants.SESSION_USER_INFO);
        if(user ==null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        
		return orderService.getOrderCartProduct(user.getId());
	}
	
	@RequestMapping(value = "detail")
	@ResponseBody
	public ServerResponse<OrderVo> detail(HttpSession session, Long orderNo) {
		User user = (User)session.getAttribute(Constants.SESSION_USER_INFO);
        if(user ==null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        
		return orderService.detail(user.getId(), orderNo);
	}
	
	@RequestMapping(value = "list")
	@ResponseBody
	public ServerResponse list(HttpSession session, 
			@RequestParam(value = "pageNum", defaultValue = "1") int pageNum, 
			@RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
		User user = (User)session.getAttribute(Constants.SESSION_USER_INFO);
        if(user ==null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        
		return orderService.list(user.getId(), pageNum, pageSize);
	}
	
	
	
	
	@RequestMapping(value = "pay")
	@ResponseBody
	public ServerResponse pay(HttpSession session, Long orderNo, HttpServletRequest request) {
		User user = (User)session.getAttribute(Constants.SESSION_USER_INFO);
        if(user ==null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        //
        String path = request.getSession().getServletContext().getRealPath("upload");
		
		return orderService.pay(orderNo, user.getId(), path);
	}
	
	@RequestMapping(value = "alipay_callback")
	@ResponseBody
	public Object alipayCallback(HttpServletRequest request) {
		Map<String, String> params = Maps.newHashMap();
		
		Map requestParams = request.getParameterMap();
		for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext();) {
			String name = (String) iter.next();
			String[] values = (String[]) requestParams.get(name);
			String valueStr = "";
			for (int i = 0; i < values.length; i ++) {
				valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
			}
			params.put(name, valueStr);
		}
		logger.info("支付宝回调，sing:{},trade_status:{},参数:{}", params.get("sing"), params.get("trade_status"), params.toString());
		//验证回调的正确性， 同时，避免重复通知
		params.remove("sign_type");
		try {
			boolean alipayRSACheckedV2 = AlipaySignature.rsaCheckV2(params, Configs.getAlipayPublicKey(), "utf-8", Configs.getSignType());
			if (!alipayRSACheckedV2) {
				return ServerResponse.createByErrorMessage("非法请求，验证不通过");
			}
		} catch (AlipayApiException e) {
			logger.error("支付宝回调异常", e);
		}
		ServerResponse response = orderService.alipayCallback(params);
		if (response.isSuccess()) {
			return Constants.AlipayCallback.RESPONSE_SUCCESS;
		}
		
		return Constants.AlipayCallback.RESPONSE_FAILED;
	}

	@RequestMapping(value = "query_order_pau_status")
	@ResponseBody
	public ServerResponse queryOrderPayStatus(HttpSession session, Long orderNo) {
		User user = (User)session.getAttribute(Constants.SESSION_USER_INFO);
        if(user ==null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        
        ServerResponse response = orderService.queryOrderPayStatus(user.getId(), orderNo);
        if (response.isSuccess()) {
        	return ServerResponse.createBySuccess(true);
        }
        return ServerResponse.createBySuccess(false);
	}
}
