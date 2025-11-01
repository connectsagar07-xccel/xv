package com.logicleaf.invplatform.config;

import org.springframework.stereotype.Component;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class StartupIdFilter implements Filter {

    private final Pattern startupIdPattern = Pattern.compile("/api/financials/([^/]+)/.*");

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        Matcher matcher = startupIdPattern.matcher(httpRequest.getRequestURI());

        if (matcher.matches()) {
            String startupId = matcher.group(1);
            request.setAttribute("startupId", startupId);
        }

        chain.doFilter(request, response);
    }
}
