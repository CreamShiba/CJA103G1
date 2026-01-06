package com.karshop.productimage.model;

import java.sql.Date;
import java.util.List;

public class ProductImageService {

    private ProductImageDAO_interface dao;

    public ProductImageService() {
        dao = new ProductImageJDBCDAO();
    }

    public ProductImageVO addProductImage(Integer prodNo, String prodName, byte[] upFile) {

        ProductImageVO piVO = new ProductImageVO();
        piVO.setProdNo(prodNo);
        piVO.setProdName(prodName);
        piVO.setUpFile(upFile);
        piVO.setUploadDate(new java.sql.Date(System.currentTimeMillis()));
        dao.insert(piVO);

        return piVO;

    }

    public ProductImageVO updateProductImage(Integer imgNo, Integer prodNo, String prodName, Date uploadDate, byte[] upFile) {

        ProductImageVO piVO = new ProductImageVO();
        piVO.setImgNo(imgNo);
        piVO.setProdNo(prodNo);
        piVO.setProdName(prodName);
        piVO.setUpFile(upFile);
        piVO.setUploadDate(uploadDate);
        dao.update(piVO);

        return piVO;
    }

    public void deleteImage(Integer imgNo) {
        dao.delete(imgNo);
    }

    public ProductImageVO getOneImage(Integer imgNo) {
        return dao.findByPrimaryKey(imgNo);
    }

    public List<ProductImageVO> getAll(){
        return dao.getAll();
    }

    public String getProdNameByProdNo(Integer prodNo) {
        return dao.getProdNameByProdNo(prodNo);
    }

}