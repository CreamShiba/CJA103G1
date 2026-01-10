package com.karshop.productimage.model;

import com.karshop.product.model.ProductVO;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProductImageJDBCDAO implements ProductImageDAO_interface {
    private static final String driver = "com.mysql.cj.jdbc.Driver";
    private static final String url = "jdbc:mysql://localhost:3306/test?serverTimezone=Asia/Taipei";
    private static final String userid = "root";
    private static final String passwd = "eagle890915";

    // 移除 prod_name，剩下 3 個問號
    private static final String INSERT_STMT = "INSERT INTO product_image (prod_no, upload_date, up_file) VALUES (?, ?, ?)";
    private static final String GET_ALL_STMT = "select* from product_image";
    private static final String GET_ONE_STMT = "select* from product_image where img_no = ?";
    private static final String DELETE = "DELETE from product_image where img_no = ?";
    // 移除 prod_name = ?
    private static final String UPDATE = "UPDATE product_image set prod_no = ?, upload_date = ?, up_file = ? where img_no = ?";

    static {
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void insert(ProductImageVO piVO) {
        String[] cols = { "img_no" };
        try (Connection con = DriverManager.getConnection(url, userid, passwd);
             PreparedStatement pstmt = con.prepareStatement(INSERT_STMT, cols);) {

            pstmt.setInt(1, piVO.getProduct().getProdNo());
            pstmt.setDate(2, piVO.getUploadDate());
            pstmt.setBytes(3, piVO.getUpFile());

            pstmt.executeUpdate();

            try(ResultSet rs = pstmt.getGeneratedKeys()){
                if(rs.next()) {
                    Integer imgNo = rs.getInt(1);
                    piVO.setImgNo(imgNo);
                }
            }

        } catch (SQLException se) {
            se.printStackTrace();
        }

    }

    @Override
    public void update(ProductImageVO piVO) {
        try (Connection con = DriverManager.getConnection(url, userid, passwd);
             PreparedStatement pstmt = con.prepareStatement(UPDATE)) {

            pstmt.setInt(1, piVO.getProduct().getProdNo());
            pstmt.setDate(2, piVO.getUploadDate());
            pstmt.setBytes(3, piVO.getUpFile());
            pstmt.setInt(4, piVO.getImgNo());

            pstmt.executeUpdate();

        } catch (SQLException se) {
            se.printStackTrace();
        }

    }

    @Override
    public void delete(Integer imgNo) {
        try (Connection con = DriverManager.getConnection(url, userid, passwd);
             PreparedStatement pstmt = con.prepareStatement(DELETE)) {

            pstmt.setInt(1, imgNo);

            pstmt.executeUpdate();

        } catch (SQLException se) {
            se.printStackTrace();
        }

    }

    @Override
    public ProductImageVO findByPrimaryKey(Integer imgNo) {
        ProductImageVO piVO = null;
        try (Connection con = DriverManager.getConnection(url, userid, passwd);
             PreparedStatement pstmt = con.prepareStatement(GET_ONE_STMT);) {

            pstmt.setInt(1, imgNo);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    ProductVO productVO = new ProductVO();
                    piVO = new ProductImageVO();
                    piVO.setImgNo(rs.getInt("img_no"));
                    productVO.setProdNo(rs.getInt("prod_no"));
                    piVO.setProduct(productVO);
                    piVO.setUploadDate(rs.getDate("upload_date"));
                    piVO.setUpFile(rs.getBytes("up_file"));
                }
            }

        } catch (SQLException se) {
            se.printStackTrace();
        }
        return piVO;
    }

    @Override
    public List<ProductImageVO> getAll() {
        List<ProductImageVO> list = new ArrayList<ProductImageVO>();
        ProductImageVO piVO = null;
        try (Connection con = DriverManager.getConnection(url, userid, passwd);
             PreparedStatement pstmt = con.prepareStatement(GET_ALL_STMT);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                piVO = new ProductImageVO();
                ProductVO productVO = new ProductVO();
                piVO.setImgNo(rs.getInt("img_no"));
                productVO.setProdNo(rs.getInt("prod_no"));
                piVO.setProduct(productVO);
                piVO.setUploadDate(rs.getDate("upload_date"));
                piVO.setUpFile(rs.getBytes("up_file"));
                list.add(piVO);

            }
        } catch (SQLException se) {
            se.printStackTrace();
        }
        return list;
    }

}