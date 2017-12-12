package com.mmall.service.impl;

import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CategoryMapper;
import com.mmall.pojo.Category;
import com.mmall.service.ICategoryService;

@Service
public class CategoryServiceImpl implements ICategoryService {
	
	private static final Logger logger = LoggerFactory.getLogger(CategoryServiceImpl.class);
	
	@Autowired
	private CategoryMapper categoryMapper;
	
	@Override
	public ServerResponse<String> addCategory(String categoryName, Integer parentId) {
		if (!StringUtils.isNotBlank(categoryName) || parentId == null) {
			return ServerResponse.createByErrorMessage("参数输入错误");
		}
		
		Category category = new Category();
		
		category.setName(categoryName);
		category.setParentId(parentId);
		category.setStatus(true);
		
		int count = categoryMapper.insert(category);
		if (count > 0) {
			return ServerResponse.createBySuccessMessage("添加商品成功");
		}
		
		return ServerResponse.createByErrorMessage("添加商品失败");
	}

	@Override
	public ServerResponse updateCategory(String categoryName, Integer categoryId) {
		if (!StringUtils.isNotBlank(categoryName) || categoryId == null) {
			return ServerResponse.createByErrorMessage("输入参数有误");
		}
		
		Category category = new Category();
		category.setId(categoryId);
		category.setName(categoryName);
		
		int count = categoryMapper.updateByPrimaryKeySelective(category);
		if (count > 0) {
			return ServerResponse.createBySuccessMessage("修改成功");
		}
		return ServerResponse.createBySuccessMessage("修改失败");
	}

	@Override
	public ServerResponse<List<Category>> selectChildCategorySameLevel(Integer parentId) {
		List<Category> categoryList = categoryMapper.selectChildSameLevel(parentId);
		
		if (CollectionUtils.isEmpty(categoryList)) {
			logger.info("为找到当前分类的子分类");
		}
		
		return ServerResponse.createBySuccess(categoryList);
	}

	@Override
	public ServerResponse<List<Integer>> selectChildDeep(Integer categoryId) {
		Set<Category> categorySet = Sets.newHashSet();
		findChildCategory(categorySet, categoryId);
		
		List<Integer> categoryIdList = Lists.newArrayList();
		if (categoryId != null) {
			for (Category cateogoryItem : categorySet) {
				categoryIdList.add(cateogoryItem.getId());
			}
		}
		
		return ServerResponse.createBySuccess(categoryIdList);
	}
	
	private Set<Category> findChildCategory(Set<Category> categorySet, Integer categoryId) {
		//查询当前的 category 是否存在
		Category category = categoryMapper.selectByPrimaryKey(categoryId);
		//首先添加当前的category
		if (category != null) {
			categorySet.add(category);
		}
		
		//查询当前 id 的子节点 id ，然后，遍历递归 (递归注意返回，避免死循环)
		List<Category> categoryList = categoryMapper.selectChildSameLevel(categoryId);
		for (Category categoryItem : categoryList) {
			findChildCategory(categorySet, categoryItem.getId());
		}
		
		return categorySet;
	}

}
