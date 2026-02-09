package com.karshop.memberInfo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class MemberInfoService {

    @Autowired
    private MemberInfoRepository repository;

//    public List<MemberInfo> getAll() {
//        return repository.findAll();
//    }

    public Page<MemberInfo> getAll(int page, int size) {
        // 設定排序方式（例如按會員編號升序），頁數從 0 開始
        Pageable pageable = PageRequest.of(page, size, Sort.by("memberNo").ascending());
        return repository.findAll(pageable);
    }

    @Transactional
    public void updateMemberInfo(MemberInfo memberInfo, MultipartFile file) throws IOException {
        // 先從資料庫取出原始物件
        MemberInfo existing = repository.findById(memberInfo.getMemberNo())
                .orElseThrow(() -> new RuntimeException("找不到該會員"));

        // 處理圖片：有新圖才覆蓋
        if (file != null && !file.isEmpty()) {
            existing.setMemberImage(file.getBytes());
        }

        // 處理其他欄位更新
        existing.setMemberName(memberInfo.getMemberName());
        existing.setMemberAccount(memberInfo.getMemberAccount());
        existing.setMemberEmail(memberInfo.getMemberEmail());
        existing.setMemberPhone(memberInfo.getMemberPhone());
        existing.setAddress(memberInfo.getAddress());
        existing.setAccountStatus(memberInfo.getAccountStatus());
        existing.setEngineerStatus(memberInfo.getEngineerStatus());
        existing.setSellerStatus(memberInfo.getSellerStatus());
        existing.setMemberUsername(memberInfo.getMemberUsername());

        // 處理密碼 (避免空白密碼覆蓋舊密碼)
        if (memberInfo.getMemberPassword() != null && !memberInfo.getMemberPassword().trim().isEmpty()) {
            existing.setMemberPassword(memberInfo.getMemberPassword());
        }


        // 註冊時間與評分通常不透過編輯表單修改，會自動維持 existing 的值
        repository.save(existing);
    }

    // 確保此方法存在
    public MemberInfo findByMemberNo(Integer no) {
        return repository.findById(no).orElse(null);
    }

    public Page<MemberInfo> findByCompositeQuery(Integer no, String kw, Integer status, Integer seller, Integer engineer, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("memberNo").ascending());
        return repository.findByCompositeQuery(no, kw, status, seller, engineer, pageable);
    }
}