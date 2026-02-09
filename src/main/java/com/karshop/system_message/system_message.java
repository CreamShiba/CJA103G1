package com.karshop.system_message;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "system_message")
public class system_message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_no", nullable = false)
    private Integer message_no;

    @Column(name = "member_no", nullable = false)
    private Integer member_no;

    @Column(name = "adm_no", nullable = false)
    private Integer adm_no;

    @Column(name = "message_content", length = 500)
    private String message_content;

    @Column(name = "message_status", nullable = false)
    private Boolean message_status = false;

    @Column(name = "message_time")
    private LocalDateTime message_time;

    public system_message() {}

    public system_message(Integer member_no, Integer adm_no, String message_content) {
        this.member_no = member_no;
        this.adm_no = adm_no;
        this.message_content = message_content;
        this.message_status = false;
        this.message_time = LocalDateTime.now();
    }

    // Getter and Setter
    public Integer getMessage_no() { return message_no; }
    public void setMessage_no(Integer message_no) { this.message_no = message_no; }

    public Integer getMember_no() { return member_no; }
    // 修正點：名稱必須與變數名一致
    public void setMember_no(Integer member_no) { this.member_no = member_no; }

    public Integer getAdm_no() { return adm_no; }
    public void setAdm_no(Integer adm_no) { this.adm_no = adm_no; }

    public String getMessage_content() { return message_content; }
    public void setMessage_content(String message_content) { this.message_content = message_content; }

    public Boolean getMessage_status() { return message_status; }
    public void setMessage_status(Boolean message_status) { this.message_status = message_status; }

    public LocalDateTime getMessage_time() { return message_time; }
    public void setMessage_time(LocalDateTime message_time) { this.message_time = message_time; }

//    @Override
//    public String toString() {
//        return "system_message [message_no=" + message_no + ", member_no=" + member_no +
//                ", message_status=" + message_status + "]";
//    }
}