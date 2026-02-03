package com.karshop.controller;

import com.karshop.members.model.MembersVO;
import com.karshop.model.repository.ForumMemberRepository; // 🟢 修正 Import
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class ChatPageController {

	@Autowired
	private ForumMemberRepository forumMemberRepository; // 🟢 修正注入的 Repository 型態

	@GetMapping("/chat")
	public String showChatPage(
			@RequestParam(value = "user", required = false) String quickUser,
			HttpSession session,
			Model model) {

		// 1. 私訊後門邏輯
		if (quickUser != null && !quickUser.isEmpty()) {
			// 🟢 使用新的 forumMemberRepository
			MembersVO member = forumMemberRepository.findByMemUsername(quickUser);
			if (member != null) {
				session.setAttribute("member", member);
				System.out.println("🚀 [私訊後門] 已切換身分為: " + quickUser);
			}
		}

		// 2. 登入檢查
		MembersVO currentMember = (MembersVO) session.getAttribute("member");
		if (currentMember == null) {
			return "redirect:/members/login?redirect=/chat";
		}

		// 3. 抓取好友列表（過濾自己）
		// 🟢 使用新的 forumMemberRepository
		List<MembersVO> allMembers = forumMemberRepository.findAll();
		List<MembersVO> friends = allMembers.stream()
				.filter(m -> !m.getMemNo().equals(currentMember.getMemNo()))
				.collect(Collectors.toList());

		// 4. 傳入頁面變數
		model.addAttribute("member", currentMember);
		model.addAttribute("userName", currentMember.getMemUsername());
		model.addAttribute("friends", friends);

		return "chat_center";
	}
}