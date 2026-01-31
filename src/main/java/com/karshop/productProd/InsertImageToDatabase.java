package com.karshop.productProd;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Date;

public class InsertImageToDatabase {

    // 數據庫連接信息
    private static final String DB_URL = "jdbc:mysql://localhost:3306/cja103g1";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "cja10318";

    public static void main(String[] args) {
        // 圖片檔案路徑
        String imagePath = "C:\\Users\\HsinHsuan\\Pictures\\123\\wawa4.jpg";

        // 產品編號
        int prodNo = 2003;

        // 插入圖片
        insertImage(imagePath, prodNo);
    }

    /**
     * 將圖片插入到 product_image 表
     * @param imagePath 圖片檔案的完整路徑
     * @param prodNo 產品編號
     */
    public static void insertImage(String imagePath, int prodNo) {
        // 讀取圖片檔案
        byte[] imageData = readImageFile(imagePath);

        if (imageData == null) {
            System.out.println("❌ 無法讀取圖片檔案！");
            return;
        }

        System.out.println("✅ 圖片檔案讀取成功，大小：" + imageData.length + " 字節");

        // 連接數據庫並插入圖片
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {

            System.out.println("✅ 數據庫連接成功");

            // SQL 插入語句
            String sql = "INSERT INTO product_image (prod_no, upload_date, up_file) VALUES (?, ?, ?)";

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                // 設置參數
                pstmt.setInt(1, prodNo);                                    // prod_no
                pstmt.setDate(2, new Date(System.currentTimeMillis()));    // upload_date (今天日期)
                pstmt.setBytes(3, imageData);                               // up_file (圖片數據)

                // 執行插入
                int affectedRows = pstmt.executeUpdate();

                if (affectedRows > 0) {
                    System.out.println("✅ 圖片插入成功！");
                    System.out.println("   產品編號: " + prodNo);
                    System.out.println("   圖片大小: " + imageData.length + " 字節");
                } else {
                    System.out.println("❌ 插入失敗，無行受影響");
                }
            }

        } catch (SQLException e) {
            System.out.println("❌ 數據庫錯誤: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 讀取圖片檔案並轉換為位元組陣列
     * @param filePath 圖片檔案路徑
     * @return 圖片的位元組陣列，如果失敗返回 null
     */
    public static byte[] readImageFile(String filePath) {
        File file = new File(filePath);

        // 檢查檔案是否存在
        if (!file.exists()) {
            System.out.println("❌ 檔案不存在: " + filePath);
            return null;
        }

        // 檢查是否是檔案（不是資料夾）
        if (!file.isFile()) {
            System.out.println("❌ 不是有效的檔案: " + filePath);
            return null;
        }

        System.out.println("📁 讀取檔案: " + filePath);
        System.out.println("📊 檔案大小: " + file.length() + " 字節");

        // 讀取檔案為位元組陣列
        byte[] imageData = new byte[(int) file.length()];

        try (FileInputStream fis = new FileInputStream(file)) {
            int bytesRead = fis.read(imageData);
            System.out.println("✅ 成功讀取 " + bytesRead + " 字節");
            return imageData;

        } catch (IOException e) {
            System.out.println("❌ 讀取檔案失敗: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 驗證圖片是否插入成功（可選）
     */
    public static void verifyImage(int prodNo) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {

            String sql = "SELECT img_no, prod_no, upload_date, LENGTH(up_file) AS file_size FROM product_image WHERE prod_no = ?";

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, prodNo);

                try (var rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        System.out.println("\n📋 驗證結果:");
                        System.out.println("   img_no: " + rs.getInt("img_no"));
                        System.out.println("   prod_no: " + rs.getInt("prod_no"));
                        System.out.println("   upload_date: " + rs.getDate("upload_date"));
                        System.out.println("   file_size: " + rs.getInt("file_size") + " 字節");
                    } else {
                        System.out.println("❌ 找不到圖片記錄");
                    }
                }
            }

        } catch (SQLException e) {
            System.out.println("❌ 驗證失敗: " + e.getMessage());
            e.printStackTrace();
        }
    }
}