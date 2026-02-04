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

		if (member == null) {
			response.put("success", false);
			response.put("message", "未登入");
			return response;
		}

		try {
			Integer myId = member.getMemNo();
			// 修正：從 JSON payload 取得 blockedId 並確保轉為 Integer
			Integer blockedId = Integer.valueOf(payload.get("blockedId").toString());

			if (!blacklistRepository.existsByUserIdAndBlockedUserId(myId, blockedId)) {
				blacklistRepository.save(new Blacklist(myId, blockedId));
			}
			response.put("success", true);
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

		if (member != null) {
			try {
				Integer blockedId = Integer.valueOf(payload.get("blockedId").toString());
				blacklistRepository.deleteByUserIdAndBlockedUserId(member.getMemNo(), blockedId);
				response.put("success", true);
			} catch (Exception e) {
				response.put("success", false);
			}
		}
		return response;
	}
}