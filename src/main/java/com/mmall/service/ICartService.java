package com.mmall.service;

import com.mmall.common.ServerResponse;
import com.mmall.vo.CartVo;

public interface ICartService {
	
	ServerResponse<CartVo> addProductToCart(Integer userId, Integer productId, Integer count);
	
	ServerResponse<CartVo> list(Integer userId);
	
	ServerResponse<CartVo> updateProductToCart(Integer userId, Integer productId, Integer count);
	
	ServerResponse<CartVo> delete(Integer userId, String productIds);
}
