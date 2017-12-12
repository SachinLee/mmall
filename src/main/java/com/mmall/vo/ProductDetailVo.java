package com.mmall.vo;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;

import com.mmall.pojo.Product;

public class ProductDetailVo extends Product {
	
	//新增属性
	private String imageHost;
	
	private Integer parentCategoryId;
	
	public ProductDetailVo() {
		
	}
	
	public ProductDetailVo(Product product) {
		try {
			BeanUtils.copyProperties(product, this);
		} catch (BeansException e) {
			e.printStackTrace();
		}
	}

	public String getImageHost() {
		return imageHost;
	}

	public void setImageHost(String imageHost) {
		this.imageHost = imageHost;
	}

	public Integer getParentCategoryId() {
		return parentCategoryId;
	}

	public void setParentCategoryId(Integer parentCategoryId) {
		this.parentCategoryId = parentCategoryId;
	}
	
	
}
