package com.karshop.jedis;

import java.util.List;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class JedisHandleMessage {

	private static JedisPool pool = JedisPoolUtil.getJedisPool();

	/**
	 * 取得歷史訊息
	 * @param sender 請求歷史紀錄的人 (我)
	 * @param receiver 對話對象 (對方，或是 "all")
	 */
	public static List<String> getHistoryMsg(String sender, String receiver) {
		String key;
		// 如果接收者是 "all"，代表要拿公頻的歷史紀錄
		if ("all".equals(receiver)) {
			key = "public_room:all";
		} else {
			// 如果是私訊，Key 的格式為 "sender:receiver"
			// 例如 "ZJB:墨羽"，代表 ZJB 這邊看到的與墨羽的對話紀錄
			key = new StringBuilder(sender).append(":").append(receiver).toString();
		}

		Jedis jedis = null;
		try {
			jedis = pool.getResource();
			// 取得該 Key 的所有紀錄
			List<String> historyData = jedis.lrange(key, 0, -1);
			return historyData;
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
		Jedis jedis = pool.getResource();

		if ("all".equals(receiver)) {
			// --- 情況 A：群聊 ---
			// 統一存在一個 Key，讓大家讀取同一份資料
			String publicKey = "public_room:all";
			jedis.rpush(publicKey, message);
		} else {
			// --- 情況 B：私訊 ---
			// 對雙方來說，都要各存著歷史聊天記錄，這樣點開視窗才看得到

			// 1. 存給發送者 (sender:receiver) -> 讓 "我" 之後看得到這則紀錄
			String senderKey = new StringBuilder(sender).append(":").append(receiver).toString();
			jedis.rpush(senderKey, message);

			// 2. 存給接收者 (receiver:sender) -> 讓 "對方" 之後看得到這則紀錄
			String receiverKey = new StringBuilder(receiver).append(":").append(sender).toString();
			jedis.rpush(receiverKey, message);
		}

		jedis.close();
	}
}