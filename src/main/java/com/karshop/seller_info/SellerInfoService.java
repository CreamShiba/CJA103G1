package com.karshop.seller_info;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service("sellerinfoservice")
public class SellerInfoService {

    @Autowired
    private SellerInfoRepository repository;

    private static final int PAGE_SIZE = 10;

    // ✅ 改為存在 static 資料夾內,不需要 WebConfig
    private static final String UPLOAD_DIR = "uploads/seller-images/";

    @Transactional
    public SellerInfo addSeller(SellerInfo seller) {
        return repository.save(seller);
    }


    @Transactional
    public SellerInfo updateSeller(SellerInfo seller) {
        SellerInfo existingSeller = repository.findById(seller.getSeller_no())
                .orElseThrow(() -> new RuntimeException("賣家不存在,編號:" + seller.getSeller_no()));

        // 基本資料更新
        if (seller.getShop_name() != null) existingSeller.setShop_name(seller.getShop_name());
        if (seller.getSeller_name() != null) existingSeller.setSeller_name(seller.getSeller_name());
        if (seller.getPhone() != null) existingSeller.setPhone(seller.getPhone());
        if (seller.getEmail() != null) existingSeller.setEmail(seller.getEmail());
        if (seller.getAddress() != null) existingSeller.setAddress(seller.getAddress());
        if (seller.getDescription() != null) existingSeller.setDescription(seller.getDescription());
        if (seller.getBank_name() != null) existingSeller.setBank_name(seller.getBank_name());
        if (seller.getBank_code() != null) existingSeller.setBank_code(seller.getBank_code());
        if (seller.getBank_account() != null) existingSeller.setBank_account(seller.getBank_account());
        if (seller.getAccount_holder() != null) existingSeller.setAccount_holder(seller.getAccount_holder());
        if (seller.getSeller_tax_id() != null) existingSeller.setSeller_tax_id(seller.getSeller_tax_id());

        // ✅ 關鍵修正：必須把 Controller 傳過來的「待審核」狀態存進去
        if (seller.getStatus() != null) {
            existingSeller.setStatus(seller.getStatus());
        }

        // 圖片路徑更新
        if (seller.getImage_path() != null) {
            existingSeller.setImage_path(seller.getImage_path());
        }

        return repository.save(existingSeller);
    }

    @Transactional
    public void deleteSeller(Integer sellerNo) {
        repository.deleteById(sellerNo);
    }

    public SellerInfo getOneSeller(Integer sellerNo) {
        return repository.findById(sellerNo)
                .orElseThrow(() -> new RuntimeException("找不到賣家,編號:" + sellerNo));
    }

    public Page<SellerInfo> getAllSellers(int pageNumber) {
        Pageable pageable = PageRequest.of(pageNumber - 1, PAGE_SIZE);
        return repository.findAll(pageable);
    }

    public int getPageTotal() {
        long totalCount = repository.count();
        return (int) Math.ceil((double) totalCount / PAGE_SIZE);
    }

    @Transactional
    public String uploadImage(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) return null;

        Path uploadPath = Paths.get(System.getProperty("user.dir"), UPLOAD_DIR);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath);

        // 返回相對路徑,配合 Controller 的映射
        return "/members/seller/uploads/seller-images/" + fileName;
    }

    public List<SellerInfo> searchByShopName(String shopName) {
        return repository.findByShopName(shopName);
    }

    public List<SellerInfo> getVerifiedSellers() {
        return repository.findByVerified(true);
    }

    public SellerInfo getSellerByMemberId(Integer memberId) {
        return repository.findByMemberNo(memberId)
                .orElseThrow(() -> new RuntimeException("此會員尚未申請成為賣家"));
    }

    /**
     * 修正所有舊的圖片路徑格式
     */
    @Transactional
    public int fixAllImagePaths() {
        List<SellerInfo> allSellers = repository.findAll();
        int fixedCount = 0;

        for (SellerInfo seller : allSellers) {
            String oldPath = seller.getImage_path();
            if (oldPath != null && oldPath.startsWith("/images/uploads/")) {
                String newPath = oldPath.replace("/images/uploads/", "/uploads/");
                seller.setImage_path(newPath);
                repository.save(seller);
                fixedCount++;
                System.out.println("修正路徑: " + oldPath + " -> " + newPath);
            }
        }

        return fixedCount;
    }
}
