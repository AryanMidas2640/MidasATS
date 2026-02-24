package com.midas.consulting.security.api;

import com.midas.consulting.service.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


public class TenantWithTokenFilter extends OncePerRequestFilter {

  private static final Logger logger = LoggerFactory.getLogger(TenantWithTokenFilter.class);
  private static final String TENANT_HEADER = "X-Tenant";

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
          throws ServletException, IOException {

    String tenantId = request.getHeader(TENANT_HEADER);

    logger.info("TenantWithTokenFilter - X-Tenant header value: {}", tenantId);

    if (tenantId != null && !tenantId.isEmpty()) {
      logger.info("Setting tenant context to: {}", tenantId);
      TenantContext.setCurrentTenant(tenantId);
    } else {

      if (request.getRequestURI().contains("webhook")||request.getRequestURI().contains("notifications")||request.getRequestURI().contains("notification")){
        TenantContext.setCurrentTenant("68cc764fbfc57730593b4a32");
        filterChain.doFilter(request, response);

      }

      logger.info("No tenant header found, setting tenant context to null");
      TenantContext.setCurrentTenant(null);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.setContentType("application/json");
      response.getWriter().write("{\"error\": \"Missing X-Tenant header\"}");
      return; // 🚫 stop processing further
    }

    try {
      filterChain.doFilter(request, response);
    } finally {
      logger.info("Clearing tenant context");
      TenantContext.clear();
    }
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getRequestURI();
    return path.startsWith("/swagger-ui/") ||
            path.equals("/swagger-ui.html") ||
            path.startsWith("/v3/api-docs/") ||
            path.equals("/v3/api-docs") ||
            path.equals("/v2/api-docs") ||
            path.startsWith("/webjars/") ||
            path.startsWith("/swagger-resources/") ||
            path.equals("/swagger-resources") ||
            path.startsWith("/configuration/") ||
            path.equals("/login") ||
            path.equals("/signup") ||
            path.equals("/");
  }
}