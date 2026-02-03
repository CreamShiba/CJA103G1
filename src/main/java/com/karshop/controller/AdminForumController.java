package com.karshop.controller;

import com.karshop.model.entity.*;
import com.karshop.model.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Controller
@RequestMapping("/admin/forum")
public class AdminForumController {

	@Autowired
	private ForumReportRepository forumReportRepository;
	@Autowired
	private ForumPostRepository forumPostRepository;
	@Autowired
	private CategoriesRepository categoriesRepository;
	@Autowired
	private CommentRepository commentRepository;
	@Autowired
	private PostFavoriteRepository postFavoriteRepository;
	@Autowired
	private PostLikeRepository postLikeRepository;

	// ==========================================
	// 1. 文章管理 (Article Management)
	// ==========================================
	@GetMapping("/article/list")
	public String adminPostList(Model model) {
		List<ForumPost> list = forumPostRepository.findAll();
		model.addAttribute("postList", list);
		return "admin_post_list";
	}

	@GetMapping("/article/edit")
	public String editPostPage(@RequestParam("postId") Integer postId, Model model) {
		ForumPost post = forumPostRepository.findById(postId).orElse(null);
		if (post != null) {
			List<Categories> allCategories = categoriesRepository.findAll();
			model.addAttribute("post", post);
			model.addAttribute("allCategories", allCategories);
			return "admin_post_edit";
		}
		return "redirect:/admin/forum/article/list";
	}

	@PostMapping("/article/update")
	public String updatePost(@ModelAttribute ForumPost post) {
		ForumPost existingPost = forumPostRepository.findById(post.getPostId()).orElse(null);
		if (existingPost != null) {
			existingPost.setTitle(post.getTitle());
			existingPost.setPostTxt(post.getPostTxt());
			if (post.getCategories() != null) {
				existingPost.setCategories(categoriesRepository.findById(post.getCategories().getCategoryId()).orElse(null));
			}
			forumPostRepository.save(existingPost);
		}
		return "redirect:/admin/forum/article/list";
	}

	@Transactional
	@PostMapping("/article/delete")
	public String deletePost(@RequestParam(value = "postId", required = false) Integer postId) {
		if (postId != null) {
			try {
				// 深度清理：按照外鍵限制順序刪除
				postLikeRepository.deleteByPostId(postId);
				postFavoriteRepository.deleteByPostId(postId);
				commentRepository.deleteByPostId(postId);
				// 也要刪除相關檢舉，否則文章刪除後檢舉會變孤兒
				forumReportRepository.deleteByPostId(postId);

				if (forumPostRepository.existsById(postId)) {
					forumPostRepository.deleteById(postId);
					System.out.println("✅ 文章 ID " + postId + " 已連鎖刪除");
				}
			} catch (Exception e) {
				System.err.println("❌ 文章刪除失敗: " + e.getMessage());
			}
		}
		return "redirect:/admin/forum/article/list";
	}

	// ==========================================
	// 2. 留言管理 (Comment Management)
	// ==========================================
	@GetMapping("/comment/list")
	public String commentList(Model model) {
		List<ForumComment> comments = commentRepository.findAll();
		model.addAttribute("comments", comments);
		return "admin_comment_list";
	}

	// 🟢 補回：編輯留言頁面跳轉
	@GetMapping("/comment/edit")
	public String editCommentPage(@RequestParam("commentId") Integer commentId, Model model) {
		ForumComment comment = commentRepository.findById(commentId).orElse(null);
		if (comment != null) {
			model.addAttribute("forumComment", comment);
			return "admin_comment_edit";
		}
		return "redirect:/admin/forum/comment/list";
	}

	// 🟢 補回：執行留言更新
	@PostMapping("/comment/update")
	public String updateComment(@ModelAttribute ForumComment forumComment) {
		ForumComment existing = commentRepository.findById(forumComment.getCommentId()).orElse(null);
		if (existing != null) {
			existing.setContent(forumComment.getContent());
			commentRepository.save(existing);
		}
		return "redirect:/admin/forum/comment/list";
	}

	@PostMapping("/comment/delete")
	public String deleteComment(@RequestParam(value = "commentId", required = false) Integer commentId) {
		if (commentId != null) {
			commentRepository.deleteById(commentId);
		}
		return "redirect:/admin/forum/comment/list";
	}

	// ==========================================
	// 3. 檢舉管理 (Report Management)
	// ==========================================
	@GetMapping("/report/list")
	public String adminReportList(Model model) {
		List<ForumReport> list = forumReportRepository.findAll();
		model.addAttribute("reportList", list);
		return "admin_report_list";
	}

	// 🟢 補回：處理「下架」與「駁回」邏輯
	@Transactional
	@PostMapping("/report/process")
	public String processReport(@RequestParam("reportId") Integer reportId, @RequestParam("status") String status) {
		ForumReport report = forumReportRepository.findById(reportId).orElse(null);
		if (report != null) {
			report.setStatus(status);

			if ("已下架".equals(status)) {
				// 🟢 現在 report.getForumPost() 會回傳一個物件，再接 .getPostId() 就完全正確了！
				if (report.getForumPost() != null) {
					Integer targetPostId = report.getForumPost().getPostId();
					deletePost(targetPostId);
				}
			}

			forumReportRepository.save(report);
		}
		return "redirect:/admin/forum/report/list";
	}

	// ==========================================
	// 4. 收藏管理 (Favorite Management)
	// ==========================================
	@GetMapping("/favorite/list")
	public String favoriteList(Model model) {
		List<PostFavorite> favList = postFavoriteRepository.findAll();
		model.addAttribute("favList", favList);
		return "admin_favorite_list";
	}

	@PostMapping("/favorite/delete")
	public String deleteFavorite(@RequestParam(value = "favId", required = false) Integer favId) {
		if (favId != null) {
			postFavoriteRepository.deleteById(favId);
		}
		return "redirect:/admin/forum/favorite/list";
	}
}