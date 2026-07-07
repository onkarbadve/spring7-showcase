package com.example.showcase.config;

import com.example.showcase.client.CatalogLookupClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.support.RestClientHttpServiceGroupConfigurer;
import org.springframework.web.service.registry.ImportHttpServices;

/**
 * Spring Framework 7's HTTP Service Group registry. This replaces the old
 * boilerplate of manually building an HttpServiceProxyFactory + RestClient
 * and registering the proxy as a @Bean by hand. Declare the interface(s) you
 * want proxied, then configure the underlying RestClient for that group.
 */
@Configuration(proxyBeanMethods = false)
@ImportHttpServices(group = "catalog-lookup", types = CatalogLookupClient.class)
public class HttpServiceClientConfig {

    private final String catalogLookupBaseUrl;

    public HttpServiceClientConfig(
            @Value("${catalog.lookup.base-url:http://localhost:${server.port:8080}/demo-upstream}") String catalogLookupBaseUrl) {
        this.catalogLookupBaseUrl = catalogLookupBaseUrl;
    }

    @Bean
    RestClientHttpServiceGroupConfigurer catalogLookupGroupConfigurer() {
        return groups -> groups.filterByName("catalog-lookup")
                .forEachClient((group, builder) -> builder.baseUrl(catalogLookupBaseUrl));
    }
}
