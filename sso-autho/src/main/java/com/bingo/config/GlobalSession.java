package com.bingo.config;

import com.bingo.pojo.TokenInfo;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

public class GlobalSession {
    public static Map<String,HttpSession> SESSIONS=new HashMap<>();

    public static void addSession(String sessionId, HttpSession session){
        SESSIONS.put(sessionId,session);
    }

    public static void destroySession(String sessionId){
        SESSIONS.remove(sessionId);
    }

    //根据id得到session
    public static HttpSession getSession(String sessionId) {
        return SESSIONS.get(sessionId);
    }

}
