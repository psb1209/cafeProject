package com.example.cafeProject.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.image.upload-dir}")
    private String uploadDir;

    @Value("${app.image.url-prefix}")
    private String urlPrefix;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String handler = urlPrefix.endsWith("/") ? urlPrefix + "**" : urlPrefix + "/**";

        String location = Paths.get(uploadDir).toUri().toString();
        location = location.endsWith("/") ? location : location + "/";

        registry.addResourceHandler(handler)
                .addResourceLocations(location);
    }

}