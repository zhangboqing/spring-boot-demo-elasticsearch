package com.zbq.springbootdemoelasticsearch.repository;

import com.alibaba.fastjson.JSON;
import com.zbq.springbootdemoelasticsearch.model.GoodsESEntity;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.aggregations.metrics.avg.InternalAvg;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;

import java.util.Arrays;
import java.util.List;

/**
 * @Author zhangboqing
 * @Date 2020-01-03
 */
@SpringBootTest
@Slf4j
public class GoodsESRepositoryTest {

    @Autowired
    private GoodsESRepository goodsESRepository;

    /**
     * 测试新增
     */
    @Test
    public void save() {
        GoodsESEntity goodsESEntity1 = new GoodsESEntity(2L, "尾灯 L 大众速腾", "国际品牌", "16D 945 095", "31231231", "16D 945 095",
                "尾灯 L 大众速腾 国际品牌 16D 945 095 31231231 16D 945 095");
        GoodsESEntity goodsESEntity2 = new GoodsESEntity(1L, "后", "国际品牌", "16D 945 095", "31231231", "16D 945 095",
                "后 国际品牌 16D 945 095 31231231 16D 945 095");
        Iterable<GoodsESEntity> goodsESEntities = goodsESRepository.saveAll(Arrays.asList(goodsESEntity1, goodsESEntity2));
        log.info("【save】= {}", goodsESEntities);
    }


    /**
     * 测试更新
     */
    @Test
    public void update() {
        goodsESRepository.findById(1L).ifPresent(goodsESEntity -> {
            goodsESEntity.setGoodsName(goodsESEntity.getGoodsName() + "\n更新更新更新更新更新");
            GoodsESEntity save = goodsESRepository.save(goodsESEntity);
            log.info("【save】= {}", save);
        });
    }

    /**
     * 测试删除
     */
    @Test
    public void delete() {
        // 主键删除
        goodsESRepository.deleteById(1L);

        // 对象删除
        goodsESRepository.findById(2L).ifPresent(goodsESEntity -> goodsESRepository.delete(goodsESEntity));

        // 批量删除
        goodsESRepository.deleteAll(goodsESRepository.findAll());
    }

    /**
     * 测试普通查询，按goodsId倒序
     */
    @Test
    public void select() {
        goodsESRepository.findAll(Sort.by(Sort.Direction.DESC, "goodsId"))
                .forEach(goodsESEntity -> log.info("【goods】: {}", JSON.toJSONString(goodsESEntity)));
    }

    /**
     * 自定义查询，根据goodsId范围查询
     */
    @Test
    public void customSelectRangeOfAge() {
        goodsESRepository.findByGoodsIdBetween(1, 2).forEach(goodsESEntity -> log.info("【goods】: {}", JSON.toJSONString(goodsESEntity)));
    }

    /**
     * 高级查询
     */
    @Test
    public void advanceSelect() {
        // QueryBuilders 提供了很多静态方法，可以实现大部分查询条件的封装
        MatchQueryBuilder queryBuilder = QueryBuilders.matchQuery("goodsName", "保时捷跑车V20");
        log.info("【queryBuilder】= {}", queryBuilder.toString());

        goodsESRepository.search(queryBuilder).forEach(goodsESEntity -> log.info("【goods】: {}", JSON.toJSONString(goodsESEntity)));
    }

    /**
     * 自定义高级查询
     */
    @Test
    public void customAdvanceSelect() {
        // 构造查询条件
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        // 添加基本的分词条件
        queryBuilder.withQuery(QueryBuilders.matchQuery("goodsName", "保时捷跑车V20"));
        // 排序条件
        queryBuilder.withSort(SortBuilders.fieldSort("goodsId").order(SortOrder.DESC));
        // 分页条件
        queryBuilder.withPageable(PageRequest.of(0, 2));
        Page<GoodsESEntity> goodsESEntities = goodsESRepository.search(queryBuilder.build());
        log.info("【people】总条数 = {}", goodsESEntities.getTotalElements());
        log.info("【people】总页数 = {}", goodsESEntities.getTotalPages());
        goodsESEntities.forEach(goodsESEntity -> log.info("【goods】= {}",JSON.toJSONString(goodsESEntity)));
    }

    /**
     * 测试聚合，测试平均goodsId
     */
    @Test
    public void avg() {
        // 构造查询条件
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        // 不查询任何结果
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{""}, null));

        // 平均goodsId
        queryBuilder.addAggregation(AggregationBuilders.avg("goodsIdAvg").field("goodsId"));

        log.info("【queryBuilder】= {}", JSON.toJSONString(queryBuilder.build()));

        AggregatedPage<GoodsESEntity> goodsESEntities = (AggregatedPage<GoodsESEntity>) goodsESRepository.search(queryBuilder.build());
        double avgGoodsId = ((InternalAvg) goodsESEntities.getAggregation("goodsIdAvg")).getValue();
        log.info("【avgGoodsId】= {}", avgGoodsId);
    }

    /**
     * 测试高级聚合查询
     */
    @Test
    public void advanceAgg() {
        // 构造查询条件
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        // 不查询任何结果
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{""}, null));

        // 1. 添加一个新的聚合，聚合类型为terms，聚合名称为goodsName，聚合字段为goodsId
        queryBuilder.addAggregation(AggregationBuilders.terms("goodsName").field("goodsName")
                // 2. 在goodsName聚合桶内进行嵌套聚合，求平均goodsId
                .subAggregation(AggregationBuilders.avg("goodsIdAvg").field("goodsId")));

        log.info("【queryBuilder】= {}", JSON.toJSONString(queryBuilder.build()));

        // 3. 查询
        AggregatedPage<GoodsESEntity> people = (AggregatedPage<GoodsESEntity>) goodsESRepository.search(queryBuilder.build());

        // 4. 解析
        // 4.1. 从结果中取出名为 goodsName 的那个聚合，因为是利用String类型字段来进行的term聚合，所以结果要强转为StringTerm类型
        StringTerms goodsName = (StringTerms) people.getAggregation("goodsName");
        // 4.2. 获取桶
        List<StringTerms.Bucket> buckets = goodsName.getBuckets();
        for (StringTerms.Bucket bucket : buckets) {
            // 4.3. 获取桶中的key，即goodsName名称  4.4. 获取桶中的文档数量
            log.info("{} 总共有 {} 个", bucket.getKeyAsString(), bucket.getDocCount());
            // 4.5. 获取子聚合结果：
            InternalAvg avg = (InternalAvg) bucket.getAggregations().asMap().get("goodsIdAvg");
            log.info("平均goodsId：{}", avg);
        }
    }

}