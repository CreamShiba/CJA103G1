package com.karshop.controller;

import com.karshop.model.entity.*;
import com.karshop.model.repository.*;
import com.karshop.members.model.MembersVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.util.*;

@Controller
@RequestMapping("/forum")
public class ForumPostController {

	@Autowired
	private ForumMemberRepository forumMemberRepository;
	@Autowired
	private ForumPostRepository forumPostRepository;
	@Autowired
	private PostFavoriteRepository postFavoriteRepository;
	@Autowired
	private PostImageRepository postImageRepository;
	@Autowired
	private CommentRepository commentRepository;
	@Autowired
	private PostLikeRepository postLikeRepository;
	@Autowired
	private CategoriesRepository categoriesRepository;

	// 1. 列表頁面
	@GetMapping
	public String list(HttpSession session, Model model) {
		List<ForumPost> list = forumPostRepository.findAll();
		model.addAttribute("forumList", list);

		MembersVO member = (MembersVO) session.getAttribute("member");
		List<Integer> myFavIds = new ArrayList<>();
		List<Integer> myLikedIds = new ArrayList<>();

		if (member != null) {
			Integer memNo = member.getMemNo();
			// 🟢 呼叫對齊後的 Repository 方法
			myFavIds = postFavoriteRepository.findPostIdsByMemberNo(memNo);
			myLikedIds = postLikeRepository.findLikedPostIdsByMemberNo(memNo);
			model.addAttribute("userName", member.getMemUsername());
		} else {
			model.addAttribute("userName", "Guest");
		}

		model.addAttribute("myFavIds", myFavIds);
		model.addAttribute("myLikedIds", myLikedIds);
		return "forum_list";
	}

	// 2. 跳轉到發文頁面
	@GetMapping("/addPage")
	public String addPage(Model model) {
		model.addAttribute("forumPost", new ForumPost());
		model.addAttribute("allCategories", categoriesRepository.findAll());
		return "forum_add";
	}

	// 3. 處理新增文章
	@PostMapping("/insert")
	public String insert(@ModelAttribute ForumPost forumPost,
						 @RequestParam("imageFiles") MultipartFile[] files,
						 HttpSession session) {

		MembersVO member = (MembersVO) session.getAttribute("member");

		if (member != null) {
			// 🟢 傳入整個 member 物件
			forumPost.setMember(member);
		} else {
			// 預設抓取 ID 1 的會員
			MembersVO defaultMember = forumMemberRepository.findById(1).orElse(null);
			forumPost.setMember(defaultMember);
		}

		ForumPost savedPost = forumPostRepository.save(forumPost);

		String uploadDir = "C:/cja103_uploads/";
		File dir = new File(uploadDir);
		if (!dir.exists()) dir.mkdirs();

		for (MultipartFile file : files) {
			if (!file.isEmpty()) {
				try {
					String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
					file.transferTo(new File(uploadDir + fileName));

					PostImage postImage = new PostImage();
					postImage.setForumPost(savedPost);
					postImage.setUrl("/uploads/" + fileName);
					postImageRepository.save(postImage);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return "redirect:/forum";
	}

	// 4. 處理刪除文章
	@PostMapping("/delete")
	public String delete(@RequestParam("postId") Integer postId) {
		forumPostRepository.deleteById(postId);
		return "redirect:/forum";
	}

	// 5. 收藏功能 (修正紅字重點)
	@PostMapping("/favorite/{postId}")
	@ResponseBody
	public String toggleFavorite(@PathVariable("postId") Integer postId, HttpSession session) {
		MembersVO member = (MembersVO) session.getAttribute("member");
		if (member == null) return "login_required";

		Integer memNo = member.getMemNo();
		// 🟢 修正：將 MemId 改為 MemNo，與 Repository 對齊
		boolean exists = postFavoriteRepository.existsByMember_MemNoAndForumPost_PostId(memNo, postId);

		if (exists) {
			// 🟢 修正：將 MemId 改為 MemNo
			postFavoriteRepository.deleteByMember_MemNoAndForumPost_PostId(memNo, postId);
			return "removed";
		} else {
			ForumPost post = forumPostRepository.findById(postId).orElse(null);
			if (post != null) {
				PostFavorite fav = new PostFavorite();
				fav.setForumPost(post);
				fav.setMember(member);
				postFavoriteRepository.save(fav);
				return "success";
			}
		}
		return "error";
	}

	// 6. 按讚功能
	@PostMapping("/like/{postId}")
	@ResponseBody
	public Map<String, Object> likePost(@PathVariable("postId") Integer postId, HttpSession session) {
		Map<String, Object> response = new HashMap<>();
		MembersVO member = (MembersVO) session.getAttribute("member");

		if (member == null) {
			response.put("success", false);
			response.put("message", "login_required");
			return response;
		}

		ForumPost post = forumPostRepository.findById(postId).orElse(null);
		if (post == null) {
			response.put("success", false);
			return response;
		}

		Integer memNo = member.getMemNo();
		// 🟢 對齊 Repository 的方法名
		boolean alreadyLiked = postLikeRepository.existsByMemberNoAndPostId(memNo, postId);

		long currentLikes = (post.getPostLike() == null) ? 0L : post.getPostLike();

		if (alreadyLiked) {
			postLikeRepository.deleteByMemberNoAndPostId(memNo, postId);
			post.setPostLike(Math.max(0L, currentLikes - 1));
			response.put("action", "unliked");
		} else {
			PostLike postLike = new PostLike();
			postLike.setMemberNo(memNo);
			postLike.setPostId(postId);
			postLikeRepository.save(postLike);

			post.setPostLike(currentLikes + 1);
			response.put("action", "liked");
		}

		forumPostRepository.save(post);
		response.put("success", true);
		response.put("newCount", post.getPostLike());

		return response;
	}

	// 7. 新增留言
	@PostMapping("/addComment")
	@ResponseBody
	public Map<String, Object> addComment(@RequestBody Map<String, String> payload, HttpSession session) {
		Map<String, Object> response = new HashMap<>();
		MembersVO member = (MembersVO) session.getAttribute("member");

		if (member == null) {
			response.put("success", false);
			response.put("message", "請先登入");
			return response;
		}

		String content = payload.get("content");
		String postIdStr = payload.get("postId");

		if (postIdStr == null || content == null) {
			response.put("success", false);
			return response;
		}

		Integer postId = Integer.parseInt(postIdStr);
		ForumPost post = forumPostRepository.findById(postId).orElse(null);

		if (post != null) {
			ForumComment comment = new ForumComment();
			comment.setContent(content);
			comment.setForumPost(post);
			comment.setMember(member);
			commentRepository.save(comment);

			response.put("success", true);
			response.put("userName", member.getMemUsername());
			response.put("content", content);
		} else {
			response.put("success", false);
		}
		return response;
	}

	// 8. 測試登入後門
	@GetMapping("/testLogin")
	public String testLogin(@RequestParam("id") Integer id, HttpSession session) {
		MembersVO member = forumMemberRepository.findById(id).orElse(null);
		if (member != null) {
			session.setAttribute("member", member);
			System.out.println("🚀 [後門啟動] 已模擬登入為: " + member.getMemName());
			return "redirect:/forum";
		} else {
			return "redirect:/members/login";
		}
	}
}