package com.demon.serviceimp;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.demon.config.EsResultMapping;
import com.demon.util.JsonUtils;
import com.demon.vo.EsTemplate;
import org.elasticsearch.action.search.ClearScrollRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageInfo;


@Service
public class PublicQueryServiceImpl {
    private Logger logger = LoggerFactory.getLogger(PublicQueryServiceImpl.class);


    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

//    @Autowired
    private EsTemplate esTemplate;

    @Autowired
    private EsResultMapping esResultMapping;

    @Resource(name="esClient")
    private Client client;

    /**
     * 	分页加排序查询
     * @param queryBuilder
     * @param pages
     * @param pageSize
     * @param orderBy
     * @param direction
     * @param clazz
     * @return
     */
    public <T> PageInfo<T> queryForPage(NativeSearchQueryBuilder queryBuilder,
                                        Integer pages,Integer pageSize,String[] orderBy,
                                        Direction direction,Class<T> clazz){

        long total = getTotal(queryBuilder);//esTemplate.count(queryBuilder.build());
        if(pages==null) {
            pages=1;
        }
        if(pageSize==null) {
            pageSize=10;
        }
        if(orderBy!=null && orderBy.length>0) {
            if(direction!=null) {
                queryBuilder.withPageable(PageRequest.of(pages-1, pageSize,Sort.by(direction, orderBy)));//分页加排序
            }else {
                queryBuilder.withPageable(PageRequest.of(pages-1, pageSize,Sort.by(orderBy)));
            }
        }else {
            queryBuilder.withPageable(PageRequest.of(pages-1, pageSize));//分页无排序
        }
        //queryBuilder.ag
        logger.info("DSL:=====>{}",queryBuilder.build().getQuery().toString());
        Page<T> page = elasticsearchTemplate.queryForPage(queryBuilder.build(),clazz,esResultMapping);
        PageInfo<T> resPage = new PageInfo<>();
        resPage.setPageNum(pages==0?1:pages);
        resPage.setPageSize(page.getSize());
        resPage.setTotal(total);
        resPage.setList(page.getContent());
        return resPage;
    }

    /**
     * 	默认分页查询无排序
     * @param queryBuilder
     * @param clazz
     * @return
     */
    public <T> PageInfo<T> queryForPage(NativeSearchQueryBuilder queryBuilder,Class<T> clazz){
        return queryForPage(queryBuilder, null,null,null,null,clazz);
    }

    /**
     * 	默认分页加排序查询
     * @param queryBuilder
     * @param clazz
     * @return
     */
    public <T> PageInfo<T> queryForPage(NativeSearchQueryBuilder queryBuilder,String[] orderBy,
                                        Direction direction,Class<T> clazz){
        return queryForPage(queryBuilder, null,null,orderBy,direction,clazz);
    }

    /**
     * 	分页无排序查询
     * @param queryBuilder
     * @param clazz
     * @return
     */
    public <T> PageInfo<T> queryForPage(NativeSearchQueryBuilder queryBuilder,Integer page,Integer pageSize,Class<T> clazz){
        return queryForPage(queryBuilder,page,pageSize,null,null,clazz);
    }

    /**
     * 	查询集合
     * @param queryBuilder
     * @param clazz
     * @return
     */
    public <T> List<T> queryForList(NativeSearchQueryBuilder queryBuilder,Class<T> clazz){
        logger.info("DSL:=====>{}",queryBuilder.build().getQuery().toString());
        return elasticsearchTemplate.queryForPage(queryBuilder.build(),clazz,esResultMapping).getContent();
    }

    public int updateById(String id,Object obj,Class clazz) {
        UpdateQuery updateQuery = new UpdateQuery();
        updateQuery.setId(id);
        updateQuery.setClazz(clazz);
        UpdateRequest request = new UpdateRequest();
        Map<String, Object> map = JsonUtils.parseJSON2Map(JSONObject.toJSONString(obj));
        map.remove("id");
        request.doc(map);
        updateQuery.setUpdateRequest(request);
        UpdateResponse res = elasticsearchTemplate.update(updateQuery);
        return Integer.parseInt(res.getId());
    }

    public long getTotal(NativeSearchQueryBuilder queryBuilder) {
        String scrollId = null;
        long total = 0l;
        String indice=null;
        try {
            indice = queryBuilder.build().getIndices().get(0);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("method===>{}","com.jiudao.ipems.es.provider.service.impl.PublicQueryServiceImpl.getTotal","errorMsg===>{}","索引或索引别名不能为空....");
            return total;
        }
        try {
            logger.info("method:=====>{}","com.jiudao.ipems.es.provider.service.impl.PublicQueryServiceImpl.getTotal");
            logger.info("dDSL:=====>{}",queryBuilder.build().getQuery().toString());
            SearchResponse response = client.prepareSearch(indice)//.setTypes("robotXjdJgz")
                    .setQuery(queryBuilder.build().getQuery())
                    .setScroll(TimeValue.timeValueMinutes(1)) //设置游标有效期
                    .setSize(1)
                    .execute().actionGet();
            scrollId = response.getScrollId();
            total = response.getHits().getTotalHits().value;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("method====>{},message=====>{}","com.jiudao.ipems.es.provider.service.impl.PublicQueryServiceImpl.PublicQueryServiceImpl.getTotal",e.getMessage());
        }finally {
            if(scrollId!=null) {
                ClearScrollRequest request = new ClearScrollRequest();
                request.addScrollId(scrollId);
                client.clearScroll(request);
            }
        }

        return total;
    }
}
