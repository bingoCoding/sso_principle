package com.bingo.controller;

import com.bingo.config.GlobalSession;
import com.bingo.utils.StringUtil;
import com.bingo.utils.TicketUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@RestController
public class TokenController {

    protected Logger logger = LoggerFactory.getLogger(getClass());

    protected Map<String, Object> map = null;

    @RequestMapping("/verify")
    public Map<String, Object> verify(String ticket, String localSessionId, String localLoginOutUrl,
                                      String globalSessionId, HttpServletRequest request) {
        map = new HashMap<>();
        String account = TicketUtil.get(ticket);
        TicketUtil.remove(ticket);
        if (StringUtil.isUnEmpty(account)) {
            logger.info("令牌认证成功");
            // TODO 保存本地会话id和退出接口到 全局会话
            HttpSession httpSession = GlobalSession.getSession(globalSessionId);
            Map<String, String> loginOutMap = null;
            if (httpSession.getAttribute("loginOutMap") != null) {
                loginOutMap = (Map<String, String>) httpSession.getAttribute("loginOutMap");// 用户已经登录的应用服务器，map<应用退出接口，应用服务器会话id>
            } else {
                loginOutMap = new HashMap<>();
                httpSession.setAttribute("loginOutMap", loginOutMap);
            }
            loginOutMap.put(localSessionId, localLoginOutUrl);
            // 返回数据
            map.put("code", 0);
            map.put("msg", "令牌认证成功!");
            //map.put("globalSessionId", globalSessionId);// 应用发送给SSO退出请求时使用(应该无需返回)，之前登录生成令牌回调已经发送了全局会话id
            map.put("account", account);
        } else {
            logger.info("令牌认证失败");
            map.put("code", 1);
            map.put("msg", "令牌认证失败");
        }
        return map;
    }
}
