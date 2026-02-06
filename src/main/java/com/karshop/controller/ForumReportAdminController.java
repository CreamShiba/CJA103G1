package com.karshop.controller;

import com.karshop.model.entity.ForumReport;
import com.karshop.model.repository.ForumReportsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.hibernate.Hibernate;

import java.util.List;

@Controller
@RequestMapping("/admin/forum/report_mock")
public class ForumReportAdminController {

	@Autowired
	private ForumReportsRepository forumReportsRepository;

	@GetMapping("/list")
	public String list(Model model) {
		try {
			// 1. 抓取所有論壇檢舉
			List<ForumReport> reportList = forumReportsRepository.findByReportsType("FORUM");

			// 🟢 除錯：確認資料庫真實抓到的筆數
			System.out.println(">>> [DEBUG] 抓取到論壇檢舉數量: " + reportList.size());

			// 2. 🛡️ 防止渲染崩潰：強迫載入關聯文章 ID
			for (ForumReport report : reportList) {
				if (report.getForumPost() != null) {
					Hibernate.initialize(report.getForumPost());
				}
			}

			model.addAttribute("reportList", reportList);
		} catch (Exception e) {
			System.err.println("❌ 載入失敗: " + e.getMessage());
			e.printStackTrace();
		}
		return "admin_report_list";
	}

	@PostMapping("/process")
	@ResponseBody
	public String process(@RequestParam Integer reportsNo,
						  @RequestParam String status) {
		try {
			forumReportsRepository.findById(reportsNo).ifPresent(report -> {
				report.setStatus(status);
				forumReportsRepository.save(report);
			});
			return "success";
		} catch (Exception e) {
			return "error";
		}
	}
}