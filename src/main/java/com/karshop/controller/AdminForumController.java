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
	@GetMapping({"/article", "/article/list"})
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
		return "redirect:/admin/forum/article";
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
		return "redirect:/admin/forum/article";
	}

	@Transactional
	@PostMapping("/article/delete")
	public String deletePost(@RequestParam(value = "postId", required = false) Integer postId) {
		if (postId != null) {
			try {
				postLikeRepository.deleteByPostId(postId);
				postFavoriteRepository.deleteByPostId(postId);
				commentRepository.deleteByPostId(postId);
				forumReportRepository.deleteByPostId(postId);
				if (forumPostRepository.existsById(postId)) {
					forumPostRepository.deleteById(postId);
				}
			} catch (Exception e) {
				System.err.println("❌ 文章刪除失敗: " + e.getMessage());
			}
		}
		return "redirect:/admin/forum/article";
	}

	// ==========================================
	// 2. 檢舉管理 (Report Management)
	// ==========================================
	@GetMapping({"/report", "/report/list"})
	public String adminReportList(Model model) {
		List<ForumReport> list = forumReportRepository.findAll();
		model.addAttribute("reportList", list);
		return "admin_report_list";
	}

	@Transactional
	@PostMapping("/report/process")
	public String processReport(@RequestParam("reportId") Integer reportId, @RequestParam("status") String status) {
		ForumReport report = forumReportRepository.findById(reportId).orElse(null);
		if (report != null) {
			report.setStatus(status);
			if ("已下架".equals(status) && report.getForumPost() != null) {
				deletePost(report.getForumPost().getPostId());
			}
			forumReportRepository.save(report);
		}
		return "redirect:/admin/forum/report";
	}

	// ==========================================
	// 3. 收藏管理 (Favorite Management)
	// ==========================================
	@GetMapping({"/favorite", "/favorite/list"})
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
		return "redirect:/admin/forum/favorite";
	}
}