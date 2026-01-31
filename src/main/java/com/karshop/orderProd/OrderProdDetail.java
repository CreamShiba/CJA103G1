package com.karshop.orderProd;

import com.product.Product;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "ord_detail")
@IdClass(OrderProdDetailId.class)
public class OrderProdDetail {

    @Id
    @Column(name = "ord_no")
    private Integer ordNo;

    @Id
    @Column(name = "prod_no")
    private Integer prodNo;

    @Column(name = "qty")      // 對應資料庫的 qty 欄位
    private Integer qty;

    @Column(name = "price")    // 對應資料庫的 price 欄位
    private Integer price;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "prod_no", referencedColumnName = "prod_no", insertable = false, updatable = false)
    private Product product;
}
