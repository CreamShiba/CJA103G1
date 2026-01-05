package com.karshop.productimage.model;

import java.io.Serializable;
import java.sql.Date;

public class ProductImageVO implements Serializable{
    private Integer imgno;
    private Integer prodno;
    private String prodname;
    private Date uploaddate;
    private byte[] upfile;


    public ProductImageVO () {

    }

    public Integer getImgno() {
        return imgno;
    }

    public void setImgno(Integer imgno) {
        this.imgno = imgno;
    }

    public Integer getProdno() {
        return prodno;
    }

    public void setProdno(Integer prodno) {
        this.prodno = prodno;
    }


    public String getProdname() {
        return prodname;
    }

    public void setProdname(String prodname) {
        this.prodname = prodname;
    }


    public Date getUploaddate() {
        return uploaddate;
    }

    public void setUploaddate(Date uploaddate) {
        this.uploaddate = uploaddate;
    }

    public byte[] getUpfile() {
        return upfile;
    }

    public void setUpfile(byte[] upfile) {
        this.upfile = upfile;
    }




}
