package com.bingo.controller;

import com.bingo.config.LocalSessions;
import com.bingo.utils.StringUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;

@Controller
public class LoginController {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @RequestMapping("/main")
    public String main(){
        return "main";
    }

    @RequestMapping("exit")
    public String exit(HttpServletRequest request){
        String ssoServer = "http://localhost:8080";
        HttpSession session = request.getSession();
        // 本地会话id，SSO回调退出时需要的参数
        String localSessionId = request.getParameter("localSessionId");
        if (StringUtil.isEmpty(localSessionId)) {// 用户点击退出
            String basePath = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort()
                    + request.getContextPath()+"/";
            // 重定向到SSO的退出接口
            return "redirect:"+ssoServer+"/loginOut?server=" + basePath;
        } else {// SSO回调退出
            logger.info("认证中心回调退出");
            // 销毁本地seesion
            HttpSession localSession = LocalSessions.get(localSessionId);
            if (localSession != null) {
                localSession.invalidate();// 销毁
                LocalSessions.remove(localSessionId);
            } else {
                logger.info("已经退出，无需重复退出！");
            }

        }
        return null;
    }
}
