package com.mmall.service;

import java.util.Map;

import com.github.pagehelper.PageInfo;
import com.mmall.common.ServerResponse;
import com.mmall.vo.OrderVo;

public interface IOrderService {
	
	ServerResponse pay(Long orderNo, Integer userId, String path);
	
	ServerResponse alipayCallback(Map<String, String> params);
	
	ServerResponse queryOrderPayStatus(Integer userId, Long orderNo);
	
	ServerResponse createOrder(Integer userId, Integer shippingId);
	
	ServerResponse<String> cancleOrder(Integer userId, Long orderNo);
	
	ServerResponse getOrderCartProduct(Integer userId);
	
	ServerResponse<OrderVo> detail(Integer userId, Long orderNo);
	
	ServerResponse<PageInfo> list(Integer userId, int pageNum, int pageSize);
	
	
	ServerResponse<PageInfo> manageList(int pageNum, int pageSize);
	
	ServerResponse<OrderVo> manageDetail(Long orderNo);
	
	ServerResponse<PageInfo> manageSearch(Long orderNo, int pageNum, int pageSize);
	
	ServerResponse<String> sendGoods(Long orderNo);
}
