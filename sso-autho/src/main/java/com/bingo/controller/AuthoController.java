package com.bingo.controller;

import com.bingo.utils.HttpUtil;
import com.bingo.utils.StringUtil;
import com.bingo.utils.TicketUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Controller
public class AuthoController {

    Logger logger= LoggerFactory.getLogger(getClass());
    /**
     * @Title:函数
     * @Description:进入登录页面
     * @param service
     * @param model
     * @param account
     * @param request
     * @return
     */
    @RequestMapping("/toLogin")
    public String toLogin(String service, Model model, @CookieValue(value = "sso", required = false) String account,
                          HttpServletRequest request) {
        if (StringUtil.isEmpty(account)) {
            // 未登录
            logger.info("SSO未登录，进入登录页面");
            request.setAttribute("service", service);
            return "login";
        } else {
            // 已经登录
            if (StringUtil.isUnEmpty(service)) {
                // 外部访问
                // ticket
                String ticket = account + System.currentTimeMillis();
                TicketUtil.put(ticket, account);
                StringBuilder url = new StringBuilder();
                url.append(service);
                if (0 <= service.indexOf("?")) {
                    url.append("&");
                } else {
                    url.append("?");
                }
                url.append("ticket=").append(ticket);
                url.append("&globalSessionId=").append(request.getSession().getId());
                logger.info("已经登录,回跳应用网站：" + url.toString());
                return "redirect:" + url.toString();
            } else {
                // 本应用访问
                logger.info("已经登录,直接访问SSO，进入系统");
                return "userCenter";
            }
        }
    }

    /**
     * @Title:函数
     * @Description:提交登录请求
     * @param account
     * @param password
     * @param service
     * @param request
     * @param response
     * @return
     * @throws IOException
     */
    @RequestMapping("/login")
    public String login(String account, String password, String service, HttpServletRequest request,
                        HttpServletResponse response) throws IOException {
        if ("admin".equals(account) && "1234".equals(password)) {
            logger.info("登录成功");
            // cookie
            Cookie cookie = new Cookie("sso", account);
            // cookie.setMaxAge(-1);// 默认-1，浏览器不持久化，关闭浏览器则cookie失效
            cookie.setPath("/");// 同服共享
            response.addCookie(cookie);
            // 是否是应用服务器重定向而来
            if (StringUtil.isUnEmpty(service)) {
                // ticket
                String ticket = account + System.currentTimeMillis();
                TicketUtil.put(ticket, account);
                StringBuilder url = new StringBuilder();
                url.append(service);
                if (0 <= service.indexOf("?")) {
                    url.append("&");
                } else {
                    url.append("?");
                }
                url.append("ticket=").append(ticket);
                url.append("&globalSessionId=").append(request.getSession().getId());
                logger.info("登录成功：回跳应用网站：" + url.toString());
                return "redirect:" + url.toString();
            } else {
                logger.error("登录成功：进入SSO用户中心");
                return "userCenter";
            }
        } else {
            logger.info("登录失败，再次进入登录页面");
            return "redirect:" + "/toLogin?service=" + service;
        }
    }

    @RequestMapping("/loginOut")
    public String loginOut(String server, HttpServletRequest request, HttpServletResponse response) {
        HttpSession httpSession = request.getSession();
        @SuppressWarnings("unchecked")
        Map<String, String> loginOutMap = (Map<String, String>) httpSession.getAttribute("loginOutMap");// 用户已经登录的应用服务器，map<局部会话id，应用退出接口>
        if (loginOutMap != null) {
            // 登出系统
            // 直接使用map遍历并在遍历中删除元素会报错ConcurrentModificationException，不能在遍历中动态修改集合,解决办法：使用Iterator
            // for (String localSessionId : loginOutMap.keySet()) {
            // 正确的方法
            Iterator<Map.Entry<String, String>> iterator = loginOutMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, String> entry = iterator.next();
                Map<String, String> params = new HashMap<>();
                params.put("localSessionId", entry.getKey());
                try {
                    logger.info("【【登出】Url：" + entry.getValue());
                    HttpUtil.http(entry.getValue(), params);
                    iterator.remove();// 删除已经退出的APP会话信息。
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            logger.info("从未登陆过或登出会话异常，重启浏览器");
        }
        // 清除cookie
        logger.info("删除sso全局cookie");
        Cookie cookie = new Cookie("sso", "");
        cookie.setMaxAge(0);// 删除
        cookie.setPath("/");// 和创建时同一个作用域
        response.addCookie(cookie);
        // 视图控制
        if (StringUtil.isUnEmpty(server)) {
            // 应用请求而来
            logger.info("SSO(APP)退出成功，返回到：" + server);
            return "redirect:" + server;
        } else {
            logger.info("SSO中心直接退出成功");
            return "redirect:toLogin";
        }

    }
}
