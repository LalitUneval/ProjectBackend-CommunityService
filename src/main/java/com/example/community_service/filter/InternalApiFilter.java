//package com.example.community_service.filter;
//
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//import java.io.IOException;
//
//@Component
//public class InternalApiFilter extends OncePerRequestFilter {
//
//    @Value("${internal.security.key:Lalit-Super-Secret-Key-2026}")
//    private String internalKey;
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest request,
//                                    HttpServletResponse response,
//                                    FilterChain filterChain) throws ServletException, IOException {
//
//        // Read X-Internal-Secret header
//        String requestKey = request.getHeader("X-Internal-Secret");
//
//        // Allow if internal key matches
//        if (internalKey != null && internalKey.equals(requestKey)) {
//            filterChain.doFilter(request, response);
//            return;
//        }
//
//        // Also allow if valid Authorization header present
//        // (requests coming from gateway with JWT are also valid)
//        String authHeader = request.getHeader("Authorization");
//        if (authHeader != null && authHeader.startsWith("Bearer ")) {
//            filterChain.doFilter(request, response);
//            return;
//        }
//
//        // Reject everything else
//        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//        response.setContentType("application/json");
//        response.getWriter().write(
//                "{\"error\": \"Unauthorized\", \"message\": \"Direct access is restricted.\"}"
//        );
//    }
//
//    @Override
//    protected boolean shouldNotFilter(HttpServletRequest request) {
//        String path = request.getServletPath();
//        return path.startsWith("/actuator") || path.startsWith("/favicon.ico");
//    }
//}
