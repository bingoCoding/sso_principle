package com.bingo.utils;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;
/**
 * @Title:TicketUtil
 * @Description:令牌缓存
 */
public class TicketUtil {

	private static Map<String, String> TICKET_CACHE = new HashMap<>();

	public static void put(String ticket, String account) {
		TICKET_CACHE.put(ticket, account);

	}

	public static String get(String ticket) {
		return TICKET_CACHE.get(ticket);

	}

	public static void remove(String ticket) {
		TICKET_CACHE.remove(ticket);

	}
}
