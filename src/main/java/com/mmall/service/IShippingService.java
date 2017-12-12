package com.mmall.service;

import java.util.List;

import com.github.pagehelper.PageInfo;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Shipping;

public interface IShippingService {
	
	ServerResponse addShipping(Integer userId, Shipping shipping);
	
	ServerResponse<String> deleteShipping(Integer userId, Integer shippingId);
	
	ServerResponse updateShipping(Integer userId, Shipping shipping);
	
	ServerResponse<Shipping> selectShipping(Integer userId, Integer shippingId);
	
	ServerResponse<PageInfo> selectListOfShipping(Integer userId, int pageNum, int pageSize);
}
