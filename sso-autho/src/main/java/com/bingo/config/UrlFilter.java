package com.bingo.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class UrlFilter implements Filter {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private String contextPath;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        contextPath = filterConfig.getServletContext().getContextPath();
        System.out.println(contextPath + " UrlFilter：创建");
    }

    @Override
    public void doFilter(ServletRequest sRequest, ServletResponse sResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) sRequest;
        HttpServletResponse response = (HttpServletResponse) sResponse;

        request.setCharacterEncoding("utf-8");
        HttpSession session = request.getSession(true);// 若存在会话则返回该会话，否则新建一个会话。
        /**##### basePath路径的保存   #####**/
        String path = request.getContextPath();
        String basePath = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + path
                + "/";
        // logger.info(basePath);
        request.setAttribute("basePath", basePath);
        /**##### 请求路径打印   #####**/
        String url = request.getServletPath();
        if (url.equals(""))
            url += "/";
        // post请求编码,交给spring过滤器
        // request.setCharacterEncoding("utf-8");// 统一编码格式
        String loginName = (String) session.getAttribute("loginName");
        /** 无需验证的 */
        String[] strs = { "/css/", "/js/", "themes", ".css", ".jpg", ".png" }; // 路径中包含这些字符串的,可以不用用检查
        // 特殊用途的路径可以直接访问
        if (strs != null && strs.length > 0) {
            for (String str : strs) {
                if (url.indexOf(str) >= 0) {
                    filterChain.doFilter(request, response);
                    return;
                }
            }
        }
        /**
         * 使用下面的方法打印出所有参数和参数值，会使中文请求出现乱码，解决办法:在上面加入request.setCharacterEncoding(
         * ) 函数
         */
        Enumeration<?> enu = request.getParameterNames();
        Map<String, String> parameterMap = new HashMap<String, String>();
        while (enu.hasMoreElements()) {
            String paraName = (String) enu.nextElement();
            parameterMap.put(paraName, request.getParameter(paraName));
        }
        logger.info("【url日志】 UrlFilter:【" + basePath.substring(0,basePath.lastIndexOf("/"))+url + "】  loginName=" + loginName + " parameterMap="
                + parameterMap);
        /** 响应计时 **/
        Long startMillis = System.currentTimeMillis();
        filterChain.doFilter(request, response);
        logger.info("【url日志】UrlFilter【" +  basePath.substring(0,basePath.lastIndexOf("/"))+url + "】  :耗时=" + (System.currentTimeMillis() - startMillis));

    }

    @Override
    public void destroy() {
        System.out.println(contextPath + " UrlFilter：销毁");
    }
}
