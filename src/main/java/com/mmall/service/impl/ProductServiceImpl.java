package com.mmall.service.impl;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.dao.ProductMapper;
import com.mmall.pojo.Product;
import com.mmall.service.IProductService;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.ProductListVo;

@Service
public class ProductServiceImpl implements IProductService {

	@Autowired
	private ProductMapper productMapper;
	
	@Override
	public ServerResponse addOrUpdateProduct(Product product) {
		if (product == null) {
			return ServerResponse.createByErrorMessage("商品信息错误");
		}
		
		if (product.getId() != null) {
			int updateCount = productMapper.updateByPrimaryKeySelective(product);
			if (updateCount > 0) {
				return ServerResponse.createBySuccessMessage("更新成功");
			}
			return ServerResponse.createByErrorMessage("更新失败");
		} else {
			int addCount = productMapper.insert(product);
			if (addCount > 0) {
				return ServerResponse.createBySuccessMessage("添加成功");
			}
			return ServerResponse.createByErrorMessage("添加商品失败");
		}
	}

	@Override
	public ServerResponse setProductStatus(Integer productId, Integer status) {
		if (productId == null || status == null) {
			return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), "参数错误");
		}
		Product product = new Product();
		product.setId(productId);
		product.setStatus(status);
		
		int updateCount = productMapper.updateByPrimaryKeySelective(product);
		if (updateCount > 0) {
			return ServerResponse.createBySuccessMessage("更新商品状态成功");
		}
		
		return ServerResponse.createBySuccessMessage("更新商品状态失败");
	}

	@Override
	public ServerResponse getList(int pageNum, int pageSize) {
		//mybatis 分页工具
		PageHelper.startPage(pageNum, pageSize);
		List<Product> productList = productMapper.selectList();
		
		List<ProductListVo> productListVoList = Lists.newArrayList();
		
		for (Product productItem : productList) {
			ProductListVo productLsitVo = assembleProductListVo(productItem);
			productListVoList.add(productLsitVo);
		}
		
		PageInfo pageResult = new PageInfo(productList);
		pageResult.setList(productListVoList);
		
		return ServerResponse.createBySuccess(pageResult);
	}
	
	private ProductListVo assembleProductListVo(Product product){
        ProductListVo productListVo = new ProductListVo();
        productListVo.setId(product.getId());
        productListVo.setName(product.getName());
        productListVo.setCategoryId(product.getCategoryId());
        productListVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix","http://img.happymmall.com/"));
        productListVo.setMainImage(product.getMainImage());
        productListVo.setPrice(product.getPrice());
        productListVo.setSubtitle(product.getSubtitle());
        productListVo.setStatus(product.getStatus());
        return productListVo;
    }

	@Override
	public ServerResponse searchProduct(String productName, Integer productId, int pageNum, int pageSize) {
		
		PageHelper.startPage(pageNum, pageSize);
		//构造模糊查询的字段
		if (StringUtils.isNotBlank(productName)) {
			productName = new StringBuilder().append("%").append(productName).append("%").toString();
		}
		List<Product> productList = productMapper.selectByNameAndProductId(productName, productId);
		
		List<ProductListVo> productListVoList = Lists.newArrayList();
		
		for (Product productItem :productList) {
			ProductListVo productListVo = assembleProductListVo(productItem);
			productListVoList.add(productListVo);
		}
		
		PageInfo pageResult = new PageInfo(productList);
		pageResult.setList(productListVoList);
		
		return ServerResponse.createBySuccess(pageResult);
	}

}
