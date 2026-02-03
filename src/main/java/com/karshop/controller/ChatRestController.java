package com.karshop.controller;

import com.karshop.model.entity.Blacklist;
import com.karshop.model.repository.BlacklistRepository;
import com.karshop.members.model.MembersVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/chat")
public class ChatRestController {

	@Autowired
	private BlacklistRepository blacklistRepository;

	@PostMapping("/addBlacklist")
	public Map<String, Object> addBlacklist(@RequestBody Map<String, Object> payload, HttpSession session) {
		Map<String, Object> response = new HashMap<>();
		MembersVO member = (MembersVO) session.getAttribute("member");
		if (member == null) { response.put("success", false); return response; }

		try {
			Integer myId = member.getMemNo();
			Integer blockedId = Integer.valueOf(payload.get("blockedId").toString());

			if (myId.equals(blockedId)) {
				response.put("success", false);
				response.put("message", "不能封鎖自己");
				return response;
			}

			// 🟢 這裡名稱正確：existsByUserId...
			if (blacklistRepository.existsByUserIdAndBlockedUserId(myId, blockedId)) {
				response.put("success", false);
				response.put("message", "已在名單中");
			} else {
				blacklistRepository.saveAndFlush(new Blacklist(myId, blockedId));
				response.put("success", true);
			}
		} catch (Exception e) {
			response.put("success", false);
			response.put("message", e.getMessage());
		}
		return response;
	}

	@PostMapping("/removeBlacklist")
	public Map<String, Object> removeBlacklist(@RequestBody Map<String, Object> payload, HttpSession session) {
		Map<String, Object> response = new HashMap<>();
		MembersVO member = (MembersVO) session.getAttribute("member");
		if (member == null) return response;

		try {
			Integer blockedId = Integer.valueOf(payload.get("blockedId").toString());

			// 🟢 關鍵修正：原本是 deleteByUserNo... 改為 deleteByUserId...
			blacklistRepository.deleteByUserIdAndBlockedUserId(member.getMemNo(), blockedId);

			response.put("success", true);
		} catch (Exception e) {
			response.put("success", false);
		}
		return response;
	}
}