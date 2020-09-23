package com.demon.controller;

import com.demon.influxdb.InfluxDB;
import org.influxdb.dto.QueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @description: TODO
 * @author: liuhao
 * @create: 2020/9/17 15:17
 */
@RestController
@RequestMapping("/inf")
public class IdController {
    Logger logger = LoggerFactory.getLogger(IdController.class);

    @Autowired
    private InfluxDB influxDB;

    @RequestMapping("/query")
    public void query(){
        logger.info("2----------"+influxDB.getUsername());
        influxDB.influxDbBuild();
        //redisUtil.setString("unikey", "");
        String command_wd = "SELECT * FROM t_test";
        QueryResult result_wd = influxDB.query(command_wd);
        logger.info(influxDB+"result_wd----------"+result_wd);
        HashMap<String, String> tagMap = new HashMap<>();
        tagMap.put("tag_age","12");
        tagMap.put("id","1");
        Map<String,Object> field = new HashMap<>();
        field.put("id",3);
        field.put("age",13);
        influxDB.insert("hjdata", tagMap, field);
        logger.info("1");
    }

}
