package com.powernode;

import com.powernode.configs.ElasticsearchConfig;
import com.powernode.dao.ProductDao;
import com.powernode.domain.Product;

import org.elasticsearch.action.admin.indices.analyze.AnalyzeAction;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.*;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Optional;

@RunWith(SpringRunner.class)
@SpringBootTest
class ApplicationTests {
    @Resource
    private ProductDao productDao;
    @Autowired
    private ElasticsearchRestTemplate template;
    @Autowired
    private ElasticsearchConfig config;

    @Test
    public void createIndex() {
        System.out.println("创建索引");
    }

    @Test
    public void deleteIndex() throws IOException {

        RestHighLevelClient client = config.elasticsearchClient();
        DeleteIndexRequest request = new DeleteIndexRequest("shop_product");
        client.indices().delete(request, RequestOptions.DEFAULT);

    }

    /**
     * 添加操作
     */
    @Test
    public void test01() {
        Product product = new Product();
        product.setId("14");
        product.setTitle("好好好");
        product.setPrice(123456);
        product.setBrand("vivo");
        product.setIntro("这是一条介绍");
        productDao.save(product);
    }


    /**
     * 查询所有和根据id查询
     */
    @Test
    public void test02() {
        ////查询所有
        //Iterable<Product> all = productDao.findAll();
        //for (Product product : all) {
        //    System.out.println(product);
        //}
        //根据id查询
        Optional<Product> byId = productDao.findById("14");
        System.out.println(byId.get());
    }

    @Test
    public void delete() {
        productDao.deleteById("14");
        productDao.delete(new Product());
    }

    /**
     * 结果排序
     */
    @Test
    public void test03() {
        Sort sort = Sort.by(Sort.Direction.DESC, "price", "id");
        Iterable<Product> all = productDao.findAll(sort);
        for (Product product : all) {
            System.out.println(product);
        }

        NativeSearchQuery build = new NativeSearchQueryBuilder()
                .withSorts(SortBuilders.fieldSort("price")
                        .order(SortOrder.DESC))
                .withSorts(SortBuilders.fieldSort("id")
                        .order(SortOrder.DESC)).build();
        SearchHits<Product> search = template.search(build, Product.class);
        for (SearchHit<Product> productSearchHit : search) {
            System.out.println(productSearchHit.getContent());
        }
    }

    /**
     * 分页查询
     */
    @Test
    public void test04() {
        //使用ProductDao的方式
        PageRequest pageRequest = PageRequest.of(0, 2);
        Page<Product> productPage = productDao.findAll(pageRequest);
        for (Product product : productPage) {
            System.out.println(product);
        }
        //使用 template模板的方式
        NativeSearchQuery query = new NativeSearchQueryBuilder()
                .withPageable(PageRequest.of(0, 2))
                .build();
        SearchHits<Product> search = template.search(query, Product.class);
        for (SearchHit<Product> productSearchHit : search) {
            System.out.println(productSearchHit.getContent());
        }
    }

    /**
     * 检索查询之精确查询
     */
    @Test
    public void test05() {
        //第一种方式
        //NativeSearchQuery：查询对象
        //QueryBuilders:查询条件构造器，构建查询条件对象
        NativeSearchQuery query = new NativeSearchQuery(QueryBuilders.termQuery("price", 6199));

        SearchHits<Product> search1 = template.search(query, Product.class);
        for (SearchHit<Product> productSearchHit : search1) {
            System.out.println(productSearchHit.getContent());
        }

        //第二种方式
        // NativeSearchQueryBuilder ：用于建造一个NativeSearchQuery查询对象,可以链式添加条件；
        NativeSearchQueryBuilder price = new NativeSearchQueryBuilder().withQuery(QueryBuilders.termQuery("price", 6199));
        NativeSearchQuery nativeSearchQuery = price.build();
        SearchHits<Product> search = template.search(nativeSearchQuery, Product.class);
        for (SearchHit<Product> productSearchHit : search) {
            System.out.println(productSearchHit.getContent());
        }

    }

    /**
     * 检索查询之模糊查询
     */
    @Test
    public void test06() {
        //查询标题中带手机的数据，并按照价格倒序排序
        NativeSearchQuery query = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.matchQuery("title", "手机"))
                .withSorts(SortBuilders.fieldSort("price").order(SortOrder.DESC))
                .build();
        SearchHits<Product> search = template.search(query, Product.class);
        for (SearchHit<Product> productSearchHit : search) {
            System.out.println(productSearchHit.getContent());
        }


    }

    /**
     * 范围查询
     */
    @Test
    public void test07() {
        NativeSearchQuery query = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.rangeQuery("price")
                        .gte("5000")
                        .lte("10000"))
                .build();
        SearchHits<Product> search = template.search(query, Product.class);
        for (SearchHit<Product> productSearchHit : search) {
            System.out.println(productSearchHit);
        }
    }

    /**
     * 关键字查询
     */
    @Test
    public void test08() {
        NativeSearchQuery build = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery("手机", "title", "intro"))
                .build();
        SearchHits<Product> search = template.search(build, Product.class);
        template.search(build, Product.class);
        for (SearchHit<Product> productSearchHit : search) {
            System.out.println(productSearchHit.getContent());
        }
    }

    /**
     * 高亮显示
     */
    @Test
    public void test09() {
        HighlightBuilder builder = new HighlightBuilder()
                .field("price")
                .preTags("<span style='color:red'>")
                .postTags("</span>");
        NativeSearchQuery build = new NativeSearchQueryBuilder()
                .withHighlightFields(new HighlightBuilder.Field("price").preTags("<span style='color:red'>")
                        .postTags("</span>"))
                .withPageable(PageRequest.of(0, 2))
                .build();
        SearchHits<Product> search = template.search(build, Product.class);
        for (SearchHit<Product> productSearchHit : search) {
            System.out.println(productSearchHit.getContent());
        }


    }

    @Test
    public void test11() {
        // 查询商品标题中符合"pro"的字样或者价格在1000~3000的商品
        new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.boolQuery()
                        .should(QueryBuilders.matchQuery("title", "pro"))
                        .should(QueryBuilders.rangeQuery("price").gte(1000).lte(3000)))
                .build();

        // 查询商品标题中符合"i7"的字样并且价格大于7000的商品
        NativeSearchQuery query = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.boolQuery()
                        .must(QueryBuilders.matchQuery("title", "i7"))
                        .must(QueryBuilders.rangeQuery("price").gt(7000)))
                .build();

    }

    @Test
    public void test12(){
        NativeSearchQuery price = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.boolQuery().filter(QueryBuilders.termQuery("price", 6199))).build();
        SearchHits<Product> search = template.search(price, Product.class);
        for (SearchHit<Product> productSearchHit : search) {
            System.out.println(productSearchHit.getContent());
        }


    }

}





