package com.example.showcase.config;

import com.example.showcase.catalog.BookController;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerTypePredicate;
import org.springframework.web.servlet.config.annotation.ApiVersionConfigurer;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Picks the path-segment strategy for API versioning: requests to the
 * catalog API look like GET /api/v1/books and GET /api/v2/books. Spring
 * Boot does NOT choose a default strategy for you - path, header, query
 * param, and media-type are all equally supported; this is a deliberate
 * per-application choice.
 *
 * The prefix is scoped to {@link BookController} only, so the unrelated
 * {@code /orders/...} endpoints (see OrderController) stay unversioned.
 *
 * Version resolution itself is applied by the DispatcherServlet to every
 * request, not just to {@code BookController}'s. The predicate on
 * {@code usePathSegment} limits resolution to {@code /api/...} paths, and
 * {@code setVersionRequired(false)} lets unversioned paths through with no
 * resolved version - BookController still effectively requires a version
 * because {@code v{version}} is baked into its path prefix.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.addPathPrefix("/api/v{version}", HandlerTypePredicate.forAssignableType(BookController.class));
    }

    @Override
    public void configureApiVersioning(ApiVersionConfigurer configurer) {
        configurer.usePathSegment(1, requestPath -> requestPath.pathWithinApplication().value().startsWith("/api/"))
                .setVersionRequired(false)
                .addSupportedVersions("1", "2");
    }
}
