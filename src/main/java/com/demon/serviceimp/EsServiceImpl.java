package com.demon.serviceimp;

import com.demon.service.EsService;
import com.demon.vo.User;
import com.github.pagehelper.PageInfo;
import org.elasticsearch.index.query.QueryBuilders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

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

    @Override
    public PageInfo<User> selectPage(User entity) {
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        queryBuilder
                .withIndices(indic)
                .withTypes(type)
                .withQuery(QueryBuilders.matchQuery("id",1));
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

}
