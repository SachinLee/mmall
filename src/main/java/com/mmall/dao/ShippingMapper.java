package com.mmall.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.mmall.pojo.Shipping;

public interface ShippingMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Shipping record);

    int insertSelective(Shipping record);

    Shipping selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Shipping record);

    int updateByPrimaryKey(Shipping record);
    
    int deleteByIdAndUserId(@Param("userId") Integer userId, @Param("shippingId") Integer shippingId);
    
    int updateByUserId(Shipping shipping);
    
    Shipping selectByIdAndUserId(@Param("userId") Integer userId, @Param("shippingId") Integer shippingId);
    
    List<Shipping> selectByUserId(Integer userId);
}