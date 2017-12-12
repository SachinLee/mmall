package com.mmall.dao;

import org.apache.ibatis.annotations.Param;

import com.mmall.pojo.User;

public interface UserMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(User record);

    int insertSelective(User record);

    User selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(User record);

    int updateByPrimaryKey(User record);
    
    int checkUserName(String userName);
    
    int checkEmail(String email);
    
    String queryQuestionByUserName(String userName);
    
    User selectLogin(@Param("username")String username, @Param("password")String password);

    int validAnswer(@Param("userName")String userName, @Param("question")String question, @Param("answer")String answer);
    
    int checkPassword(@Param("password")String password, @Param("userId")int userId);

    int checkEmailByUserId(@Param("email")String email, @Param("userId")int userId);
}