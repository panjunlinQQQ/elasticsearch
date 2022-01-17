package com.powernode.dao;

import com.powernode.domain.Product;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

/**
 * @author 潘俊霖
 * @version 1.8
 * @create 2022-01-01-10:27
 */
@Repository
public interface ProductDao extends ElasticsearchRepository<Product,String> {
}
