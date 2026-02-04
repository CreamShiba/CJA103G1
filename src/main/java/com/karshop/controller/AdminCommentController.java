package com.karshop.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.karshop.model.entity.ForumComment;
import com.karshop.model.repository.CommentRepository;

@Controller
@RequestMapping("/admin/forum/comment") // 🟢 修正：確保對應 HTML 裡的 /admin/forum/comment
public class AdminCommentController {

	@Autowired
	private CommentRepository commentRepository;

	// 🟢 支援 /admin/forum/comment 以及 /admin/forum/comment/list
	@GetMapping({"", "/list"})
	public String listComments(Model model) {
		List<ForumComment> comments = commentRepository.findAll();
		model.addAttribute("comments", comments);
		return "admin_comment_list";
	}

	@GetMapping("/edit")
	public String editCommentPage(@RequestParam("commentId") Integer commentId, Model model) {
		ForumComment comment = commentRepository.findById(commentId).orElse(null);
		if (comment != null) {
			model.addAttribute("forumComment", comment);
			return "admin_comment_edit";
		}
		return "redirect:/admin/forum/comment";
	}

	@PostMapping("/update")
	public String updateComment(@ModelAttribute ForumComment forumComment) {
		ForumComment existing = commentRepository.findById(forumComment.getCommentId()).orElse(null);
		if (existing != null) {
			existing.setContent(forumComment.getContent());
			commentRepository.save(existing);
		}
		return "redirect:/admin/forum/comment";
	}

	@PostMapping("/delete")
	public String deleteComment(@RequestParam("commentId") Integer commentId) {
		if (commentId != null) {
			commentRepository.deleteById(commentId);
		}
		return "redirect:/admin/forum/comment";
	}
}