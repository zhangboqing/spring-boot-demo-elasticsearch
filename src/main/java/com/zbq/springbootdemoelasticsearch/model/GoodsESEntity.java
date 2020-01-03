package com.zbq.springbootdemoelasticsearch.model;

import com.zbq.springbootdemoelasticsearch.constants.EsConsts;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 * @author zhangboqing
 * @date 2019-12-06
 */
@Document(indexName = EsConsts.INDEX_NAME_MERCURY, type = EsConsts.DEFAULT_TYPE_NAME, shards = 1, replicas = 0)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GoodsESEntity {
    // 筛选条件包括：商品名称，品牌，规格，适用车型，商品编号，原厂编号

    /**
     * 主键,商品ID
     */
    @Id
    private Long goodsId;

    /**
     * 商品名称
     */
    @Field(type = FieldType.Keyword)
    private String goodsName;
    /**
     * 品牌
     */
    @Field(type = FieldType.Keyword)
    private String goodBrand;
    /**
     * 规格
     */
    @Field(type = FieldType.Keyword)
    private String goodsSpec;
    /**
     * 商品编号
     */
    @Field(type = FieldType.Keyword)
    private String goodsAccessoriesCode;
    /**
     * 原厂编号
     */
    @Field(type = FieldType.Keyword)
    private String goodsOriginalFactoryCode;

    /**
     * 复合字段，会被分词后存储
     */
    @Field(type = FieldType.Text, analyzer = "ik_smart")
    private String groupData;
}
