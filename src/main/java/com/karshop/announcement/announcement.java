package com.karshop.announcement;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity(name = "Announcement")
@Table(name = "announcement")
public class announcement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "announcement_no", nullable = false)
    private Integer announcement_no;

    @Column(name = "title", length = 200)
    private String title;

    @Column(name = "content", length = 200)
    private String content;

    @Column(name = "create_time", updatable = false)
    @CreationTimestamp
    private LocalDateTime create_time;

    @Column(name = "update_time")
    @UpdateTimestamp
    private LocalDateTime update_time;

    @Column(name = "adm_no", nullable = false)
    private Integer adm_no;

    @Column(name = "isnew", nullable = false)
    private Boolean isnew = true;

    @Column(name = "status", length = 20)
    private String status = "草稿";

    // 無參數建構子（JPA 需要）
    public announcement() {
    }

    // Getter and Setter
    public Integer getAnnouncement_no() {
        return announcement_no;
    }

    public void setAnnouncement_no(Integer announcement_no) {
        this.announcement_no = announcement_no;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreate_time() {
        return create_time;
    }

    public void setCreate_time(LocalDateTime create_time) {
        this.create_time = create_time;
    }

    public LocalDateTime getUpdate_time() {
        return update_time;
    }

    public void setUpdate_time(LocalDateTime update_time) {
        this.update_time = update_time;
    }

    public Integer getAdm_no() {
        return adm_no;
    }

    public void setAdm_no(Integer adm_no) {
        this.adm_no = adm_no;
    }

    public Boolean getIsNew() {
        return isnew;
    }

    public void setIsNew(Boolean isNew) {
        this.isnew = isnew;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

//    @Override
//    public String toString() {
//        return "announcement [announcement_no=" + announcement_no + ", title=" + title + ", content=" + content
//                + ", create_time=" + create_time +
//                ", update_time=" + update_time + ", adm_no=" + adm_no + ", isNew=" + isnew + ", status=" + status + "]";
//    }
}
