package com.smartstadium.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class RateLimitConfig implements WebMvcConfigurer {

    private final Bucket bucket;

    public RateLimitConfig() {
        Bandwidth limit = Bandwidth.classic(
            10,
            Refill.greedy(10, Duration.ofMinutes(1))
        );
        this.bucket = Bucket.builder().addLimit(limit).build();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry
            .addInterceptor(new RateLimitInterceptor(bucket))
            .addPathPatterns("/api/route", "/api/ai/chat");
    }

    private static class RateLimitInterceptor implements HandlerInterceptor {

        private final Bucket bucket;

        public RateLimitInterceptor(Bucket bucket) {
            this.bucket = bucket;
        }

        @Override
        public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
        ) throws Exception {
            if (bucket.tryConsume(1)) {
                return true;
            } else {
                response.setStatus(429);
                response.setContentType("application/problem+json");

                String jsonResponse = """
                    {
                        "type": "about:blank",
                        "title": "Too Many Requests",
                        "status": 429,
                        "detail": "API rate limit exceeded. Please try again later.",
                        "instance": "%s"
                    }
                    """.formatted(request.getRequestURI());

                response.getWriter().write(jsonResponse);
                return false;
            }
        }
    }
}
