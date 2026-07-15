package com.greenbutton.backend.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class LoginRateLimitFilter extends OncePerRequestFilter {

    // ponytail: in-memory, single-instance, resets on restart — use Bucket4j+Redis if multi-instance
    private final ConcurrentHashMap<String, List<Long>> attempts = new ConcurrentHashMap<>();
    private static final int MAX_ATTEMPTS = 5;
    private static final long WINDOW_MS = 60_000;

    public void clearAll() {
        attempts.clear();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().equals("/api/auth/login");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String ip = request.getRemoteAddr();
        long now = System.currentTimeMillis();
        AtomicBoolean limited = new AtomicBoolean(false);

        attempts.compute(ip, (k, timestamps) -> {
            List<Long> list = timestamps == null ? new ArrayList<>() : timestamps;
            list.removeIf(t -> now - t > WINDOW_MS);
            if (list.size() >= MAX_ATTEMPTS) {
                limited.set(true);
            } else {
                list.add(now);
            }
            return list;
        });

        if (limited.get()) {
            response.setStatus(429);
            return;
        }
        chain.doFilter(request, response);
    }
}
