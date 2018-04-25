package com.bingo.config;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.bingo.utils.StringUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import org.springframework.context.annotation.Configuration;
/**
 * 
 * @Title:SSOClientFilter SSO客户端过滤器
 * @Description:应用服务器必须添加的过滤器
 */

@Configuration
public class SSOClientFilter implements Filter {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private String loginPage;
	private String validateTicketUrl;
	private String localExitUrl;
	private String needLoginUrls;// 需登录拦截的url，使用逗号分隔

	@Override
	public void destroy() {
	}

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
			throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) servletRequest;
		HttpServletResponse response = (HttpServletResponse) servletResponse;
		HttpSession session = request.getSession();
		String username = (String) session.getAttribute("username");
		String ticket = request.getParameter("ticket");
		String globalSessionId = request.getParameter("globalSessionId");
		String url = request.getRequestURL().toString();

		String[] needLoginAry = needLoginUrls.split(",");

		// 除了包含needLoginAry的请求，其他的都不拦截
		if (needLoginAry != null && needLoginAry.length > 0) {
			boolean contains = false;
			for (String str : needLoginAry) {
				if (url.contains(str)) {
					contains = true;
					break;
				}
			}
			if (!contains) {
				logger.info("{}不包含隐私内容，无需SSO拦截",url);
				filterChain.doFilter(request, response);
				return;
			}
		}
		// 登录拦截
		if (StringUtil.isEmpty(username)) {// 本地未登录
			// 中心已经登录
			if (StringUtil.isUnEmpty(ticket)) {
				if (StringUtil.isUnEmpty(globalSessionId)) {
					// 令牌验证
					// 发送请求参数
					PostMethod postMethod = new PostMethod(validateTicketUrl);
					postMethod.addParameter("ticket", ticket);
					postMethod.addParameter("globalSessionId", globalSessionId);//服务端需要它找到全局会话以保存本地会话id和退出接口
					// localLoginOutUrl
					ServletContext context = request.getServletContext();// 容器
					// String localExitUrl =
					// context.getInitParameter("localExitUrl");
					String basePath = request.getScheme() + "://" + request.getServerName() + ":"
							+ request.getServerPort() + request.getContextPath() + "/";
					postMethod.addParameter("localLoginOutUrl", basePath + localExitUrl);// 退出接口
					postMethod.addParameter("localSessionId", session.getId());// 退出接口
					// 发送验证请求
					HttpClient httpClient = new HttpClient();
					try {
						httpClient.executeMethod(postMethod);
						String json = postMethod.getResponseBodyAsString();
						postMethod.releaseConnection();
						// 返回
						Map<String, Object> map = new Gson().fromJson(json, new TypeToken<Map<String, Object>>() {
						}.getType());
						int code = ((Double) map.get("code")).intValue();
						if (code == 0) {
							username = (String) map.get("account");
							session.setAttribute("username", username);
							session.setAttribute("globalSessionId", globalSessionId);// 等退出全局登录时使用
							filterChain.doFilter(request, response);
							logger.info("验票成功");
							return;
						} else {
							logger.info("验票失败，重新跳转到sso登录页面");
							response.sendRedirect(loginPage + "?service=" + url);
						}

					} catch (Exception e) {
						e.printStackTrace();
					}

				} else {
					logger.info("缺少必须的globalSessionId");
					throw new RuntimeException("ticket不为空时，globalSessionId不能为空");
				}

			} else {
				logger.info("ticket为空！跳转到认证中心登录页");
				response.sendRedirect(loginPage + "?service=" + url);
			}
		} else {// 已经登录
			logger.info("已经登录，无需拦截,进入系统:" + request.getContextPath());
			filterChain.doFilter(request, response);
			return;
		}

	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		String ssoServer = "http://localhost:8080";
		localExitUrl = "/exit";
		needLoginUrls = "/main";

		loginPage = ssoServer + "/toLogin";
		validateTicketUrl = ssoServer + "/verify";
	}
}