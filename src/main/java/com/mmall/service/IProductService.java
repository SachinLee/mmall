package com.mmall.service;

import com.mmall.common.ServerResponse;
import com.mmall.pojo.Product;

public interface IProductService {
	
	ServerResponse addOrUpdateProduct(Product product);
	
	ServerResponse setProductStatus(Integer productId, Integer status);
	
	ServerResponse getList(int pageNum, int pageSize);
	
	ServerResponse searchProduct(String productName, Integer productId, int pageNum, int pageSize);
}
