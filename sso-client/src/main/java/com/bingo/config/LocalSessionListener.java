package com.bingo.config;

import org.springframework.context.annotation.Configuration;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;


/**
 * @Title:GlobalSessionListener
 * @Description:会话监听器，监听会话的创建和销毁
 */

@Configuration
public class LocalSessionListener implements HttpSessionListener {

	@Override
	public void sessionCreated(HttpSessionEvent event) {
		HttpSession httpSession = event.getSession();
		LocalSessions.put(httpSession.getId(), httpSession);

	}

	@Override
	public void sessionDestroyed(HttpSessionEvent event) {
		LocalSessions.remove(event.getSession().getId());
	}

}
