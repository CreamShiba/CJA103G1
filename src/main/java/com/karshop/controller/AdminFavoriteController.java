package com.karshop.controller;

import com.karshop.model.entity.PostFavorite;
import com.karshop.model.repository.PostFavoriteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List; // 🟢 確保 List 有被引用

@Controller
@RequestMapping("/admin/favorites")
public class AdminFavoriteController {

	@Autowired
	private PostFavoriteRepository postFavoriteRepository;

	// 1. 顯示所有收藏清單
	@GetMapping
	public String listFavorites(Model model) {
		// 這裡會自動撈出關聯的 Member 和 ForumPost 資料
		List<PostFavorite> list = postFavoriteRepository.findAll();
		model.addAttribute("favList", list);
		return "admin_favorite_list";
	}

	// 2. 刪除收藏
	@PostMapping("/delete")
	public String deleteFavorite(@RequestParam("favId") Integer favId) {
		postFavoriteRepository.deleteById(favId);
		return "redirect:/admin/favorites";
	}
}