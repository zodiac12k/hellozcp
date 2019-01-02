package com.sk.zcp.log;

import java.io.IOException;
import java.util.UUID;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.MDC;

import ch.qos.logback.classic.ClassicConstants;

public class MdcLoggingFilter implements Filter {

	public void destroy() {
        // do nothing
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        insertIntoMDC(request);
        try {
            chain.doFilter(request, response);
        } finally {
            clearMDC();
        }
    }
    
	protected void insertIntoMDC(ServletRequest request) {
		MDC.put(ClassicConstants.REQUEST_REMOTE_HOST_MDC_KEY, request.getRemoteHost());
		
		String uuid = MDC.get("UUID");
		if (uuid == null) {
			MDC.put("UUID", UUID.randomUUID().toString());
		}

        if (request instanceof HttpServletRequest) {
            HttpServletRequest httpServletRequest = (HttpServletRequest) request;
            MDC.put(ClassicConstants.REQUEST_REQUEST_URI, httpServletRequest.getRequestURI());
            StringBuffer requestURL = httpServletRequest.getRequestURL();
            if (requestURL != null) {
                MDC.put(ClassicConstants.REQUEST_REQUEST_URL, requestURL.toString());
            }
            MDC.put(ClassicConstants.REQUEST_METHOD, httpServletRequest.getMethod());
            MDC.put(ClassicConstants.REQUEST_QUERY_STRING, httpServletRequest.getQueryString());
            MDC.put(ClassicConstants.REQUEST_USER_AGENT_MDC_KEY, httpServletRequest.getHeader("User-Agent"));
            MDC.put(ClassicConstants.REQUEST_X_FORWARDED_FOR, httpServletRequest.getHeader("X-Forwarded-For"));
        }
	}
	
	void clearMDC() {
//		MDC.remove("UUID");
        MDC.remove(ClassicConstants.REQUEST_REMOTE_HOST_MDC_KEY);
        MDC.remove(ClassicConstants.REQUEST_REQUEST_URI);
        MDC.remove(ClassicConstants.REQUEST_QUERY_STRING);
        // removing possibly inexistent item is OK
        MDC.remove(ClassicConstants.REQUEST_REQUEST_URL);
        MDC.remove(ClassicConstants.REQUEST_METHOD);
        MDC.remove(ClassicConstants.REQUEST_USER_AGENT_MDC_KEY);
        MDC.remove(ClassicConstants.REQUEST_X_FORWARDED_FOR);
    }

    public void init(FilterConfig arg0) throws ServletException {
    }
}
