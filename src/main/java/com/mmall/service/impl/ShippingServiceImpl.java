package com.mmall.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Maps;
import com.mmall.common.ServerResponse;
import com.mmall.dao.ShippingMapper;
import com.mmall.pojo.Shipping;
import com.mmall.service.IShippingService;

@Service
public class ShippingServiceImpl implements IShippingService {
	@Autowired
	private ShippingMapper shippingMapper;
	
	@Override
	public ServerResponse addShipping(Integer userId, Shipping shipping) {
		shipping.setUserId(userId);
		
		int rowCount = shippingMapper.insert(shipping);
		if (rowCount > 0) {
			Map result = Maps.newHashMap();
			result.put("shippingId", shipping.getId());
			return ServerResponse.createBySuccess("新建地址成功", result);
		}
		
		return ServerResponse.createByErrorMessage("新建地址失败");
	}

	@Override
	public ServerResponse<String> deleteShipping(Integer userId, Integer shippingId) {
		int rowCount = shippingMapper.deleteByIdAndUserId(userId, shippingId);
		if (rowCount > 0 ) {
			return ServerResponse.createBySuccess("删除地址成功");
		}
		
		return ServerResponse.createByErrorMessage("删除地址失败");
	}
	
	@Override
	public ServerResponse updateShipping(Integer userId, Shipping shipping) {
		//依然需要设置，userId为当前用户，避免传输的shipping 中，userId 错误，即不是当前用户；
		shipping.setUserId(userId);
		int rowCount = shippingMapper.updateByUserId(shipping);
		if (rowCount > 0) {
			return ServerResponse.createBySuccess("更新地址成功");
		}
		
		return ServerResponse.createByErrorMessage("更新地址失败");
	}

	@Override
	public ServerResponse<Shipping> selectShipping(Integer userId, Integer shippingId) {
		Shipping shipping = shippingMapper.selectByIdAndUserId(userId, shippingId);
		if (shipping != null) {
			return ServerResponse.createBySuccess(shipping);
		}
		
		return ServerResponse.createByErrorMessage("获取地址失败");
	}

	@Override
	public ServerResponse<PageInfo> selectListOfShipping(Integer userId, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize); //分页
		//查询所有信息
		List<Shipping> shippingList = shippingMapper.selectByUserId(userId);
		
		PageInfo pageInfo = new PageInfo(shippingList);
		
		return ServerResponse.createBySuccess(pageInfo);
	}
}
