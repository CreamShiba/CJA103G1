package com.karshop.productimage.model;

import java.io.Serializable;
import java.sql.Date;

public class ProductImageVO implements Serializable {
    private Integer imgNo;
    private Integer prodNo;
    private String prodName;
    private Date uploadDate;
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

    public String getProdName() {
        return prodName;
    }

    public void setProdName(String prodName) {
        this.prodName = prodName;
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