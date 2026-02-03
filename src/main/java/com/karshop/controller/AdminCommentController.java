package com.karshop.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.karshop.model.entity.ForumComment;
import com.karshop.model.repository.CommentRepository;

@Controller
@RequestMapping("/admin/comments")
public class AdminCommentController {

	@Autowired
	private CommentRepository commentRepository;

	@GetMapping
	public String listComments(Model model) {
		// 🟢 只要你的 ForumComment.java 已經把 Member 換成 MembersVO，這裡就不會紅
		List<ForumComment> comments = commentRepository.findAll();
		model.addAttribute("comments", comments);
		return "admin_comment_list";
	}

	@PostMapping("/update")
	public String updateComment(@RequestParam("id") Integer commentId,
								@RequestParam("content") String content) {
		ForumComment comment = commentRepository.findById(commentId).orElse(null);
		if (comment != null) {
			comment.setContent(content);
			commentRepository.save(comment);
		}
		return "redirect:/admin/comments";
	}

	@PostMapping("/delete")
	public String deleteComment(@RequestParam("id") Integer commentId) {
		commentRepository.deleteById(commentId);
		return "redirect:/admin/comments";
	}
}