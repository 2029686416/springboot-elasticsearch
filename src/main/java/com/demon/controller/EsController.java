package com.demon.controller;

import com.demon.dom.UserDom;
import com.demon.service.EsService;
import com.demon.vo.User;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @description: TODO
 * @author: liuhao
 * @create: 2020/8/27 17:17
 */
@RestController
@RequestMapping("/es")
public class EsController {
    @Autowired
    private EsService esService;

    @RequestMapping("/sp")
    public PageInfo<User> selectPage(@RequestBody User user){
        PageInfo<User> pageInfo = esService.selectPage(user);
        return pageInfo;
    }

//    @RequestMapping("/selectById")
//    public UserDom selectById(@RequestBody User user){
//        UserDom us = esService.selectById(user);
//        return us;
//    }

    @RequestMapping("/add")
    public int add(@RequestBody UserDom userDom){
        return esService.insertUser(userDom);
    }

}
