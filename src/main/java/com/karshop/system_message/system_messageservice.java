package com.karshop.system_message;

import com.karshop.members.model.MembersRepository;
import com.karshop.members.model.MembersVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service("system_messageservice")
public class system_messageservice {

    @Autowired
    private system_messagerepository repository;

    @Autowired
    private MembersRepository membersRepository;

    /**
     * 取得特定會員的所有通知
     */
    public List<system_message> getMessagesByMember(Integer memberNo) {
        return repository.findByMember_noOrderByMessage_timeDesc(memberNo);
    }

    /**
     * 取得所有通知（後台用）
     */
    public List<system_message> getAllMessages() {
        return repository.findAllOrderByMessage_timeDesc();
    }

    /**
     * 根據 ID 取得單一通知
     */
    public system_message getOneMessage(Integer id) {
        return repository.findById(id).orElse(null);
    }

    /**
     * 新增單一通知
     */
    @Transactional
    public void createMessage(system_message entity) {
        entity.setMessage_time(LocalDateTime.now());
        entity.setMessage_status(false);
        repository.save(entity);
    }

    /**
     * 發送通知給全體會員
     */
    @Transactional
    public void sendToAllMembers(Integer adm_no, String content) {
        List<MembersVO> allMembers = membersRepository.findAll();
        for (MembersVO member : allMembers) {
            system_message msg = new system_message();
            msg.setMember_no(member.getMemNo());
            msg.setAdm_no(adm_no);
            msg.setMessage_content(content);
            msg.setMessage_status(false);
            msg.setMessage_time(LocalDateTime.now());
            repository.save(msg);
        }
    }

    /**
     * 更新通知內容
     */
    @Transactional
    public void updateMessage(Integer id, system_message entity) {
        repository.findById(id).ifPresent(existing -> {
            existing.setMessage_content(entity.getMessage_content());
            repository.save(existing);
        });
    }

    /**
     * 刪除通知
     */
    @Transactional
    public void deleteMessage(Integer id) {
        repository.deleteById(id);
    }

    /**
     * 🚩 新增：標記單一通知為已讀
     */
    @Transactional
    public void markAsRead(Integer id) {
        repository.findById(id).ifPresent(msg -> {
            msg.setMessage_status(true);
            repository.save(msg);
        });
    }

    /**
     * 🚩 新增：標記特定會員的所有通知為已讀
     */
    @Transactional
    public void markAllAsReadByMember(Integer memberNo) {
        List<system_message> unreadMessages = repository.findUnreadMessagesByMember_no(memberNo);
        for (system_message msg : unreadMessages) {
            msg.setMessage_status(true);
            repository.save(msg);
        }
    }

    /**
     * 🚩 新增：取得特定會員的未讀通知數量
     */
    public Long getUnreadCountByMember(Integer memberNo) {
        return repository.countUnreadMessagesByMember_no(memberNo);
    }
}