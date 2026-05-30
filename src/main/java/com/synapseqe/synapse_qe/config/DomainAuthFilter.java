package com.synapseqe.synapse_qe.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.URI;

@Component
@RequiredArgsConstructor
@Slf4j
public class DomainAuthFilter extends OncePerRequestFilter {

    private final JdbcTemplate jdbcTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication instanceof JwtAuthenticationToken jwtToken) {
            String userId = jwtToken.getToken().getSubject(); // Sub usually holds the User ID in Neon Auth JWTs
            
            String origin = request.getHeader("Origin");
            if (origin == null) {
                 origin = request.getHeader("Referer");
            }

            if (origin != null) {
                try {
                    URI uri = new URI(origin);
                    String host = uri.getHost();
                    if (host != null) {
                        String slug = extractSlugFromHost(host);
                        
                        // Check organization mapping
                        boolean isAuthorized = verifyUserOrganization(userId, slug);
                        if (!isAuthorized) {
                            log.warn("User {} attempted to access unauthorized organization domain slug: {}", userId, slug);
                            response.sendError(HttpServletResponse.SC_FORBIDDEN, "User does not have access to this domain's organization.");
                            return;
                        }
                    }
                } catch (Exception e) {
                    log.error("Failed to parse origin/referer for domain verification: {}", origin, e);
                }
            }
        }
        
        filterChain.doFilter(request, response);
    }

    private String extractSlugFromHost(String host) {
        if (host == null) return "prod";
        
        if (host.equals("localhost") || host.startsWith("127.0.0.1")) {
            return "qa";
        }
        
        if (host.startsWith("qa.")) {
            return "qa";
        }
        
        // Handle production: synapse-qe.co.uk or www.synapse-qe.co.uk
        if (host.equals("synapse-qe.co.uk") || host.equals("www.synapse-qe.co.uk")) {
            return "prod";
        }

        // Fallback for Vercel preview URLs or other variations
        String[] parts = host.split("\\.");
        if (parts.length > 0) {
            return parts[0];
        }
        return "prod";
    }

    private boolean verifyUserOrganization(String userId, String slug) {
        String sql = """
            SELECT count(*) 
            FROM neon_auth.member m 
            JOIN neon_auth.organization o ON m."organizationId" = o.id 
            WHERE m."userId" = ? AND o.slug = ?
        """;
        
        try {
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId, slug);
            return count != null && count > 0;
        } catch (Exception e) {
            log.error("Error verifying user organization mapping in Neon Auth", e);
            return false;
        }
    }
}
