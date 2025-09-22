package com.example.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final String uploadDir;

    public WebConfig(FileUploadProperties properties) {
        this.uploadDir = properties.getUploadDir();
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:5173")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
    
    // ✨ [추가] 업로드된 파일에 대한 URL 매핑
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 'file:///C:/uploads/' 와 같은 실제 파일 경로
        String resourceLocation = "file:///" + System.getProperty("user.dir").replace("\\", "/") + "/" + uploadDir.replace("./", "");

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(resourceLocation);
    }
}