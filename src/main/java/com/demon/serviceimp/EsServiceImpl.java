package com.demon.serviceimp;

import com.alibaba.fastjson.JSONObject;
import com.demon.dom.UserDom;
import com.demon.repository.UserRespository;
import com.demon.service.EsService;
import com.demon.util.JsonUtils;
import com.demon.vo.User;
import com.github.pagehelper.PageInfo;
import org.apache.commons.beanutils.BeanUtils;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @description: TODO
 * @author: liuhao
 * @create: 2020/8/27 17:49
 */
@Service
public class EsServiceImpl extends PublicQueryServiceImpl  implements EsService {
    private Logger logger = LoggerFactory.getLogger(EsServiceImpl.class);
    private final static String indic="test1";
    private final static String type="type1";

    @Autowired
    private UserRespository userRespository;
    @Resource(name="esClient")
    private Client client;
    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @Override
    public List<UserDom> selectUsertlist(UserDom entity) {
        SearchResponse response = client.prepareSearch(indic).setTypes(type)
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                //.setQuery(QueryBuilders.termQuery("dbptbh", entity.getDbptbh())) // Query
                //.setPostFilter(QueryBuilders.rangeQuery("age").from(2).to(11)) // Filter
                .setFrom(0).setSize(10)
                .setExplain(true).execute().actionGet();
        SearchHits hits = response.getHits();
        List<UserDom> list = new ArrayList<>();
        for (SearchHit searchHit : hits) {
            Map<String,Object> source = searchHit.getSourceAsMap();
            UserDom dom = (UserDom) JSONObject.parseObject(JSONObject.toJSONString(source),UserDom.class);
            list.add(dom);
        }
        return list;
    }

    @Override
    public List<UserDom> IndexResponse(UserDom entity) {
//        String json = JSONObject.toJSON(entity).toString();
//        IndexResponse response = client.prepareIndex("test1","type1")
//                .setSource(json, XContentType.JSON)
//                .execute().actionGet();
//        System.out.println(response.status());
        return null;
    }

    @Override
    public PageInfo<User> selectPage(User entity) {
        //构建查询条件
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        queryBuilder
                .withIndices(indic)
                .withTypes(type)
                .withQuery(QueryBuilders.matchQuery("id",1));//匹配查询
        //.withPageable(PageRequest.of(0, 10,Sort.by(Direction.DESC, "jhzxsj","tjsj")));//分页加排序
        PageInfo<User> resPage = queryForPage(queryBuilder, 1,10, null, Sort.Direction.DESC, User.class);

//		Page<RobotXjdJgzDom> page = elasticsearchTemplate.queryForPage(queryBuilder.build(),RobotXjdJgzDom.class,esResultMapping);
//		PageInfo<RobotXjdJgzDom> resPage = new PageInfo<>();
//		resPage.setPageNum(page.getNumber());
//		resPage.setPageSize(page.getSize());
//		resPage.setTotal(page.getTotalElements());
//		resPage.setList(page.getContent());
        return resPage;
    }

    @Override
    public UserDom selectById(UserDom entity) {
        UserDom dom = new UserDom();
        dom.setId(Integer.valueOf(entity.getId()));
        return userRespository.findById(dom.getId()).get();
    }

    @Override
    public int insertUser(UserDom userDom) {
        userRespository.save(userDom);
        return 1;
    }

    @Override
    public int updateUser(UserDom entity) {
        UserDom dom = new UserDom();
        int det = 0;
        try {
            BeanUtils.copyProperties(dom, entity);
            dom = userRespository.save(dom);
            UpdateQuery updateQuery = new UpdateQuery();
            updateQuery.setId(entity.getId().toString());
            updateQuery.setClazz(UserDom.class);
            UpdateRequest request = new UpdateRequest();
            Map<String, Object> map = JsonUtils.parseJSON2Map(JSONObject.toJSONString(dom));
            request.doc(map);
            updateQuery.setUpdateRequest(request);
            UpdateResponse res = elasticsearchTemplate.update(updateQuery);
            det = Integer.parseInt(res.getId());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return det;
    }

    @Override
    public int deleteUser(UserDom entity) {
        UserDom dom = new UserDom();
        try {
            BeanUtils.copyProperties(dom, entity);
            userRespository.delete(dom);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        return 1;
    }

    @Override
    public int deleteUsertById(UserDom entity) {
        userRespository.deleteById(entity.getId());
        return 1;
    }

}
