package servlet;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * 简单的权限过滤器：
 * - 限制 /admin/*、/purchase/*、/report/* 必须是管理员登录
 */
public class AuthFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) {
        // no-op
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpSession session = httpRequest.getSession(false);

        // 只拦截后台相关路径（在 web.xml 中具体配置）
        String role = null;
        if (session != null) {
            Object roleObj = session.getAttribute("currentRole");
            if (roleObj instanceof String) {
                role = (String) roleObj;
            }
        }

        if (!"ADMIN".equals(role)) {
            // 未以管理员身份登录，重定向到登录页
            if (session != null) {
                session.setAttribute("warningMessage", "请先以管理员身份登录后再访问后台功能");
            }
            String contextPath = httpRequest.getContextPath();
            httpResponse.sendRedirect(contextPath + "/jsp/auth/login.jsp?role=admin");
            return;
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // no-op
    }
}


