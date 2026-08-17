package com.portafolio_manager.backend.infrastructure.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class HttpLoggingFilter extends OncePerRequestFilter {

    private static final Logger logger =
            LoggerFactory.getLogger(HttpLoggingFilter.class);

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            filterChain.doFilter(request, response);
        } finally {
            logRequest(request, response);
        }
    }

    private void logRequest(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String message = String.format(
                "HTTP %s %s -> %d",
                request.getMethod(),
                request.getRequestURI(),
                response.getStatus()
        );

        int status = response.getStatus();

        if (status >= 500) {
            logger.error(message);
        } else if (status >= 400) {
            logger.warn(message);
        } else {
            logger.info(message);
        }
    }
}
