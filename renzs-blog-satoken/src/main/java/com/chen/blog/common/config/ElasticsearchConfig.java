package com.chen.blog.common.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchDataAutoConfiguration;
import org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchRestClientAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Loads Elasticsearch only when Elasticsearch search is selected.
 */
@Configuration
@ConditionalOnProperty(name = "search.mode", havingValue = "elasticsearch")
@Import({
        ElasticsearchRestClientAutoConfiguration.class,
        ElasticsearchDataAutoConfiguration.class
})
public class ElasticsearchConfig {
}
