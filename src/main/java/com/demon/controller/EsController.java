package com.demon.controller;

import com.demon.dom.UserDom;
import com.demon.service.EsService;
import com.demon.vo.User;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

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

    @RequestMapping(value = "/test", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_VALUE)
    public List<UserDom> test(@RequestBody UserDom entity, HttpServletRequest request) {
        List<UserDom> list=esService.IndexResponse(entity);
        return list;
    }

    @RequestMapping(value = "/selectlist", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_VALUE)
    public List<UserDom> selectRobotAlertlist(@RequestBody UserDom entity, HttpServletRequest request) {
        List<UserDom> list=esService.selectUsertlist(entity);
        return list;
    }

    @RequestMapping("/sp")
    public PageInfo<User> selectPage(@RequestBody User user){
        PageInfo<User> pageInfo = esService.selectPage(user);
        return pageInfo;
    }

    //分组查看
    @RequestMapping("/fz")
    public int selectfz(@RequestBody User user){
        return esService.selectfz(user);
    }

    @RequestMapping(value = "/selectById", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_VALUE)
    public UserDom selectById(@RequestBody UserDom entity,HttpServletRequest request) {
        UserDom _entity=esService.selectById(entity);
        return _entity;
    }

    @RequestMapping("/add")
    public int add(@RequestBody UserDom userDom){
        return esService.insertUser(userDom);
    }

    @RequestMapping(value = "/update", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_VALUE)
    public int update(@RequestBody UserDom entity,HttpServletRequest request) {
        return esService.updateUser(entity);
    }

    @RequestMapping(value = "/delete", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_VALUE)
    public int deleteRobotAlert(@RequestBody UserDom entity,HttpServletRequest request) {
        return esService.deleteUser(entity);
    }

    @RequestMapping(value = "/deleteById", method = {RequestMethod.POST},
            produces = MediaType.APPLICATION_JSON_VALUE)
    public int deleteRobotAlertById(@RequestBody UserDom entity,HttpServletRequest request) {
        return esService.deleteUsertById(entity);
    }

}
