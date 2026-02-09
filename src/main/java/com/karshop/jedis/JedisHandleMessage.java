package com.karshop.jedis;

import java.util.List;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class JedisHandleMessage {

	private static JedisPool pool = JedisPoolUtil.getJedisPool();

	/**
	 * 🟢 核心工具：產生唯一的聊天室 Key (路徑洗衣機)
	 * 1. 統一處理公頻關鍵字 (public_room, all)
	 * 2. 私訊自動排序名字，確保 A:B 和 B:A 對應到同一個 Key
	 */
	public static String buildKey(String sender, String receiver) {
		// 1. 防呆判斷：這些關鍵字都視為公頻
		if ("public_room".equals(receiver) || "all".equals(receiver) ||
				"public_room".equals(sender) || "all".equals(sender)) {
			return "public_room";
		}

		// 2. 私訊排序邏輯 (String 的 compareTo 方法)
		// 這樣不管是 (UserA, UserB) 還是 (UserB, UserA) 進來，都會變成 "UserA:UserB"
		if (sender.compareTo(receiver) < 0) {
			return sender + ":" + receiver;
		} else {
			return receiver + ":" + sender;
		}
	}

	/**
	 * 取得歷史訊息
	 */
	public static List<String> getHistoryMsg(String sender, String receiver) {
		// 直接呼叫洗衣機，拿到正確的 Key
		String key = buildKey(sender, receiver);

		Jedis jedis = null;
		try {
			jedis = pool.getResource();
			// 取得該 Key 的所有紀錄
			return jedis.lrange(key, 0, -1);
		} finally {
			if (jedis != null) {
				jedis.close();
			}
		}
	}

	/**
	 * 儲存聊天訊息
	 */
	public static void saveChatMessage(String sender, String receiver, String message) {

		String key = buildKey(sender, receiver);

		Jedis jedis = null;
		try {
			jedis = pool.getResource();
			// 因為 Key 只有一個 (正規化了)，所以只要存一次就好
			jedis.rpush(key, message);
		} finally {
			if (jedis != null) {
				jedis.close();
			}
		}
	}
}