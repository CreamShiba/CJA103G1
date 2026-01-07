package com.karshop.productimage.model;

import jakarta.persistence.*;

import java.io.Serializable;
import java.sql.Date;

@Entity
@Table(name = "product_image")
public class ProductImageVO implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "img_no")
    private Integer imgNo;

   @Column(name = "prod_no")
    private Integer prodNo;

    @Column(name = "upload_date")
    private Date uploadDate;

    @Column(name = "up_file")
    private byte[] upFile;

    public ProductImageVO() {

    }

    public Integer getImgNo() {
        return imgNo;
    }

    public void setImgNo(Integer imgNo) {
        this.imgNo = imgNo;
    }

    public Integer getProdNo() {
        return prodNo;
    }

    public void setProdNo(Integer prodNo) {
        this.prodNo = prodNo;
    }

    public Date getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(Date uploadDate) {
        this.uploadDate = uploadDate;
    }

    public byte[] getUpFile() {
        return upFile;
    }

    public void setUpFile(byte[] upFile) {
        this.upFile = upFile;
    }
}