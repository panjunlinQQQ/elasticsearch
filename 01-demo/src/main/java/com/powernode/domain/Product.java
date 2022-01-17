package com.powernode.domain;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 @Document：配置操作哪个索引下的哪个类型
 @Id：标记文档ID字段
 @Field：配置映射信息，如：分词器
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName="es_shop")
public class Product {
    @Id
    private String id;

    @Field(analyzer="ik_max_word",searchAnalyzer="ik_max_word", type= FieldType.Text)
    private String title;

    private Integer price;

    @Field(analyzer="ik_max_word",searchAnalyzer="ik_max_word", type=FieldType.Text)
    private String intro;

    @Field(type=FieldType.Keyword)
    private String brand;
}
