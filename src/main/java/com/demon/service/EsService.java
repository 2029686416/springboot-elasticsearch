package com.demon.service;

import com.demon.dom.UserDom;
import com.demon.vo.User;
import com.github.pagehelper.PageInfo;

import java.util.List;

/**
 * @description: TODO
 * @author: liuhao
 * @create: 2020/8/27 17:13
 */
public interface EsService{
    List<UserDom> selectUsertlist(UserDom entity);

    PageInfo<User> selectPage(User entity);

    UserDom selectById(UserDom entity);

    int insertUser(UserDom userDom);

    int updateUser(UserDom userDom);

    int deleteUser(UserDom userDom);

    int deleteUsertById(UserDom userDom);
}
