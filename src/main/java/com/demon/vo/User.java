package com.demon.vo;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;

/**
 * @description: TODO
 * @author: liuhao
 * @create: 2020/8/27 17:08
 */
@Document(indexName = "school",type = "student", shards = 1,replicas = 0, refreshInterval = "-1")
public class User {
    @Id
    private String id;
    @Field
    private String name;
    @Field
    private Integer age = 0;
    @Field
    private String introduce;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getIntroduce() {
        return introduce;
    }

    public void setIntroduce(String introduce) {
        this.introduce = introduce;
    }
}
