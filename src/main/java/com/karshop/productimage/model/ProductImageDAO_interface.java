package com.karshop.productimage.model;

import java.util.List;

public interface ProductImageDAO_interface {
    public void insert(ProductImageVO piVO);
    public void update(ProductImageVO piVO);
    public void delete(Integer imgNo);
    public ProductImageVO findByPrimaryKey(Integer imgNo);
    //	public List<ProductImageVO> getAllImageByProdNo(Integer prodNo);
    public List<ProductImageVO> getAll();
}


