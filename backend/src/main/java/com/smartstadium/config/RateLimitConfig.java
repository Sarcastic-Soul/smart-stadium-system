package com.smartstadium.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.net.URI;
import java.time.Duration;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
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
        private final ObjectMapper objectMapper = new ObjectMapper();

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

                ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                    HttpStatus.TOO_MANY_REQUESTS,
                    "API rate limit exceeded. Please try again later."
                );
                problemDetail.setType(URI.create("about:blank"));
                problemDetail.setTitle("Too Many Requests");
                problemDetail.setInstance(URI.create(request.getRequestURI()));

                String jsonResponse = objectMapper.writeValueAsString(problemDetail);
                response.getWriter().write(jsonResponse);
                return false;
            }
        }
    }
}
