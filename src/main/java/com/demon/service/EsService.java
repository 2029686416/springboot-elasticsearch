package com.demon.service;

import com.demon.dom.UserDom;
import com.demon.vo.User;
import com.github.pagehelper.PageInfo;

/**
 * @description: TODO
 * @author: liuhao
 * @create: 2020/8/27 17:13
 */
public interface EsService{

    PageInfo<User> selectPage(User entity);

//    UserDom selectById(User entity);

    int insertUser(UserDom userDom);

}
