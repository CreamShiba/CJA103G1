package com.karshop.system_message;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service("system_messageservice")
public class system_messageservice {

    @Autowired
    private system_messagerepository repository;

    /**
     * 取得特定會員的所有通知（前台用）
     */
    public List<system_message> getMessagesByMemberNo(Integer member_no) {
        return repository.findByMember_noOrderByMessage_timeDesc(member_no);
    }

    /**
     * 取得特定會員的未讀通知
     */
    public List<system_message> getUnreadMessages(Integer member_no) {
        return repository.findUnreadMessagesByMember_no(member_no);
    }

    /**
     * 取得特定會員的未讀通知數量
     */
    public Long getUnreadCount(Integer member_no) {
        return repository.countUnreadMessagesByMember_no(member_no);
    }

    /**
     * 取得所有通知（後台用）
     * @return 所有系統通知
     */
    public List<system_message> getAllMessages() {
        return repository.findAllOrderByMessage_timeDesc();
    }

    /**
     * 根據 ID 取得單一通知
     */
    public system_message getMessageById(Integer id) {
        return repository.findById(id).orElse(null);
    }

    /**
     * 新增系統通知
     */
    @Transactional
    public system_message createMessage(Integer member_no, Integer adm_no, String content) {
        system_message message = new system_message(member_no, adm_no, content);
        return repository.save(message);
    }

    /**
     * 新增系統通知（使用 Entity）
     */
    @Transactional
    public system_message createMessage(system_message entity) {
        if (entity.getMessage_time() == null) {
            entity.setMessage_time(LocalDateTime.now());
        }
        if (entity.getMessage_status() == null) {
            entity.setMessage_status(false);
        }
        return repository.save(entity);
    }

    /**
     * 標記通知為已讀
     * @param id 通知編號
     * @return 更新後的通知物件
     */
    @Transactional
    public system_message markAsRead(Integer id) {
        return repository.findById(id).map(message -> {
            message.setMessage_status(true);
            return repository.save(message);
        }).orElse(null);
    }

    /**
     * 標記特定會員的所有通知為已讀
     */
    @Transactional
    public void markAllAsReadByMemberNo(Integer memberNo) {
        List<system_message> unreadMessages = repository.findUnreadMessagesByMember_no(memberNo);
        for (system_message message : unreadMessages) {
            message.setMessage_status(true);
            repository.save(message);
        }
    }

    /**
     * 更新通知內容
     */
    @Transactional
    public system_message updateMessage(Integer id, system_message entity) {
        return repository.findById(id).map(existingMessage -> {
            existingMessage.setMessage_content(entity.getMessage_content());
            existingMessage.setMessage_status(entity.getMessage_status());
            return repository.save(existingMessage);
        }).orElse(null);
    }

    /**
     * 刪除通知
     * @param id 通知編號
     * @return 是否刪除成功
     */
    @Transactional
    public boolean deleteMessage(Integer id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return true;
        }
        return false;
    }

    /**
     * 批量發送通知給多個會員
     */
    @Transactional
    public void sendBulkMessages(List<Integer> member_nos, Integer adm_no, String content) {
        for (Integer memberNo : member_nos) {
            createMessage(memberNo, adm_no, content);
        }
    }
}