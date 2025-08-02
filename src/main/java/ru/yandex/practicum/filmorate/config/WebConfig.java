package ru.yandex.practicum.filmorate.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Перенаправление только для UI путей
        registry.addViewController("/").setViewName("forward:/index.html");
        registry.addViewController("/films").setViewName("forward:/index.html");
        registry.addViewController("/users").setViewName("forward:/index.html");
        registry.addViewController("/reviews").setViewName("forward:/index.html");
        registry.addViewController("/genres").setViewName("forward:/index.html");
        registry.addViewController("/mpa").setViewName("forward:/index.html");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(0);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                // Для разработки можно использовать один из вариантов:

                // Вариант 1: Разрешить конкретные домены (рекомендуется)
                .allowedOrigins(
                        "http://localhost:3000",  // React/Vue dev server
                        "http://127.0.0.1:3000",
                        "http://localhost:8080"   // Ваш сервер, если нужно
                )

                // ИЛИ Вариант 2: Использовать allowedOriginPatterns с шаблоном (Spring 5.3+)
                // .allowedOriginPatterns("*")

                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                // Разрешаем credentials только если используете конкретные домены
                .allowCredentials(true)
                .maxAge(3600);
    }
}