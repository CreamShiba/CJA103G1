package com.karshop.controller;

import java.time.LocalDateTime;
import java.sql.Timestamp;
import com.karshop.model.entity.*;
import com.karshop.model.repository.*;
import com.karshop.members.model.MembersVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ForumDataLoader implements CommandLineRunner {

	@Autowired
	private ForumPostRepository forumPostRepository;
	@Autowired
	private PostImageRepository postImageRepository;
	@Autowired
	private ForumMemberRepository forumMemberRepository;
	@Autowired
	private CategoriesRepository categoriesRepository;

	@Override
	@Transactional
	public void run(String... args) throws Exception {
		// 如果已經有資料，就不重複執行
		if (forumPostRepository.count() > 0) return;

		System.out.println("🛠️ 正在重新初始化論壇正確資料...");

		MembersVO m1 = forumMemberRepository.findById(1).orElse(null);
		Categories c1 = categoriesRepository.findById(1).orElse(null);

		if (m1 != null && c1 != null) {
			ForumPost p1 = new ForumPost();
			p1.setTitle("避震器改裝心得");
			p1.setPostTxt("這次換了這組避震器，路感真的變得很清晰！");

			// 設定時間
			p1.setPostDate(Timestamp.valueOf(LocalDateTime.now()));

			p1.setPostLike(15L);
			p1.setMember(m1);
			p1.setCategories(c1);
			forumPostRepository.save(p1);

			PostImage img1 = new PostImage();
			img1.setForumPost(p1);

			// 🟢 這裡原本是 rims.jpg，我幫你改成你要的 2001.jpg 了！
			// 同時加上 /images/ 前綴，確保路徑正確
			img1.setUrl("/images/2001.jpg");

			postImageRepository.save(img1);

			System.out.println("✅ 假資料修復完成！圖片已設定為 2001.jpg");
		}
	}
}