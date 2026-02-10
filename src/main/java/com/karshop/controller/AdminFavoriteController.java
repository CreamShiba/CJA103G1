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

import java.util.List;

@Controller
@RequestMapping("/admin/forum/favorite") // 主路徑
public class AdminFavoriteController {

	@Autowired
	private PostFavoriteRepository postFavoriteRepository;

	// 🟢 1. 專門處理 /list 的請求 (這就是解決 "No static resource" 的關鍵)
	@GetMapping("/list")
	public String listFavorites(Model model) {
		List<PostFavorite> list = postFavoriteRepository.findAll();
		model.addAttribute("favList", list);

		// 🟢 2. 這裡回傳的字串，必須跟你的 HTML 檔名一模一樣！
		return "admin_article_favorite";
	}

	// 為了保險，如果有人只打 /admin/forum/favorite 也能通
	@GetMapping
	public String index(Model model) {
		return listFavorites(model); // 直接轉去上面的邏輯
	}

	// 🟢 3. 刪除功能
	@PostMapping("/delete")
	public String deleteFavorite(@RequestParam("favId") Integer favId) {
		postFavoriteRepository.deleteById(favId);

		// 4. 刪除後，導回列表頁 (注意這裡要加 /list)
		return "redirect:/admin/forum/favorite/list";
	}
}