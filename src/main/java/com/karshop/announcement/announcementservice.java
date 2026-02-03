package com.karshop.announcement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service("announcementService")
public class announcementservice {

    @Autowired
    private announcementrepository repository;
    /**
     * 取得所有已發布的 FAQ（前台用）
     */
    public List<announcement> getPublishedAnnouncements() {
        return repository.findByStatus("發佈");
    }
    // 取得所有公告
    public List<announcement> getAllAnnouncements() {
        return repository.findAll();
    }

    // 根據ID取得公告
    public Optional<announcement> getAnnouncementById(Integer id) {
        return repository.findById(id);
    }

    // 新增公告
    public announcement createAnnouncement(announcement announcement) {
        return repository.save(announcement);
    }

    // 更新公告
    public announcement updateAnnouncement(Integer id, announcement announcement) {
        if (repository.existsById(id)) {
            announcement.setAnnouncement_no(id);
            return repository.save(announcement);
        }
        return null;
    }

    // 刪除公告
    public boolean deleteAnnouncement(Integer id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return true;
        }
        return false;
    }

    // 根據管理員編號查詢
    public List<announcement> getAnnouncementsByAdmNo(Integer admNo) {
        return repository.findByAdm_no(admNo);
    }

    // 根據狀態查詢
    public List<announcement> getAnnouncementsByStatus(String status) {
        return repository.findByStatus(status);
    }

    // 根據是否為新公告查詢
    public List<announcement> getNewAnnouncements() {
        return repository.findByIsnew(true);  // ← 改成 findByIsnew
    }
}