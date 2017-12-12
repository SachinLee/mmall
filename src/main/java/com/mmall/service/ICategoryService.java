package com.mmall.service;

import java.util.List;

import com.mmall.common.ServerResponse;
import com.mmall.pojo.Category;

public interface ICategoryService {
	
	ServerResponse<String> addCategory(String categoryName, Integer parentId);

	ServerResponse updateCategory(String categoryName, Integer categoryId);

	ServerResponse<List<Category>> selectChildCategorySameLevel(Integer parentId);

	ServerResponse<List<Integer>> selectChildDeep(Integer categoryId);
}
