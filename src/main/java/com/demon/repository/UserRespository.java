package com.demon.repository;

import com.demon.dom.UserDom;
import com.demon.vo.User;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRespository extends ElasticsearchRepository<UserDom,Integer> {

}
