package com.karshop.orderProd;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.karshop.productProd.ProductProd;
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

    @Column(name = "quantity")
    private Integer qty;

    @Column(name = "price")
    private Integer price;

    @ManyToOne(fetch = FetchType.EAGER)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"}) // 防止 Lazy Loading 報錯
    @JoinColumn(name = "prod_no", referencedColumnName = "prod_no", insertable = false, updatable = false)
    private ProductProd productProd;
}
