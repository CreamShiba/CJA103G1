package com.karshop.controller;

import com.karshop.model.entity.PostImage;
import com.karshop.model.repository.PostImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin/images")
public class AdminImageRestController {

	@Autowired
	private PostImageRepository postImageRepository;

	@DeleteMapping("/delete/{imageId}")
	public Map<String, Object> deleteImage(@PathVariable("imageId") Integer imageId) {
		Map<String, Object> response = new HashMap<>();

		try {
			PostImage postImage = postImageRepository.findById(imageId).orElse(null);
			if (postImage != null) {
				// 1. 取得圖片的實體路徑 (假設你的上傳目錄是 C:/cja103_uploads/)
				// url 格式通常是 /uploads/uuid_filename.png
				String url = postImage.getUrl();
				String fileName = url.substring(url.lastIndexOf("/") + 1);
				String physicalPath = "C:/cja103_uploads/" + fileName;

				// 2. 刪除硬碟中的實體檔案
				File file = new File(physicalPath);
				if (file.exists()) {
					boolean deleted = file.delete();
					System.out.println("實體檔案刪除結果: " + deleted + " (" + physicalPath + ")");
				}

				// 3. 刪除資料庫中的紀錄
				postImageRepository.delete(postImage);

				response.put("success", true);
			} else {
				response.put("success", false);
				response.put("message", "找不到該圖片紀錄");
			}
		} catch (Exception e) {
			response.put("success", false);
			response.put("message", "刪除失敗: " + e.getMessage());
		}
		return response;
	}
}