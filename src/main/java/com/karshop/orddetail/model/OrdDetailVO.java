package com.karshop.orddetail.model;

import com.karshop.ord.model.OrdVO;
import com.karshop.product.model.ProductVO;
import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(name = "ord_detail")
public class OrdDetailVO implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "detail_no")
    private Integer detailNo;

    @ManyToOne
    @JoinColumn(name = "ord_no")
    private OrdVO order;

    @ManyToOne
    @JoinColumn(name = "prod_no")
    private ProductVO product;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "price")
    private Integer price;

    public Integer getDetailNo() {
        return detailNo;
    }

    public void setDetailNo(Integer detailNo) {
        this.detailNo = detailNo;
    }

    public OrdVO getOrder() {
        return order;
    }

    public void setOrder(OrdVO order) {
        this.order = order;
    }

    public ProductVO getProduct() {
        return product;
    }

    public void setProduct(ProductVO product) {
        this.product = product;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }
}
