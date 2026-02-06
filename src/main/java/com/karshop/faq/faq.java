package com.karshop.faq;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity(name = "faq")
@Table(name = "faq")
public class faq {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "faq_no", nullable = false)
    private Integer faq_no;

    @Column(name = "question", length = 100,nullable = false)
    private String question;

    @Column(name = "answer", length = 100)
    private String answer;

    @Column(name = "status",length = 20)
    private String status;

    @Column(name = "create_date", updatable = false)
    @CreationTimestamp
    private LocalDateTime create_date;

    @Column(name = "updated_date")
    @UpdateTimestamp
    private LocalDateTime updated_date;

    @Column(name = "adm_no")
    private Integer adm_no;

    // 無參數建構子（JPA 需要）
    public faq() {
    }

    // Getter and Setter
    public Integer getFaq_no() {
        return faq_no;
    }

    public void setFaq_no(Integer faq_no) {
        this.faq_no = faq_no;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreate_date() {
        return create_date;
    }

    public void setCreate_date(LocalDateTime create_date) {
        this.create_date = create_date;
    }

    public LocalDateTime getUpdated_date() {
        return updated_date;
    }

    public void setUpdated_date(LocalDateTime updated_date) {
        this.updated_date = updated_date;
    }

    public Integer getAdm_no() {
        return adm_no;
    }

    public void setAdm_no(Integer adm_no) {
        this.adm_no = adm_no;
    }

//    @Override
//    public String toString() {
//        return "faq [faqNo=" + faq_no +
//                ", question=" + question +
//                ", answer=" + answer +
//                ", status=" + status +
//                ", create_date=" + create_date +
//                ", updated_date=" + updated_date +
//                ", adm_no=" + adm_no + "]";
//    }
}
