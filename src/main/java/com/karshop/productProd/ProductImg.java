package com.karshop.productProd;

import jakarta.persistence.*;
import lombok.Data;

import java.sql.Date;

@Data
@Entity
@Table(name = "product_image")
public class ProductImg {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "img_no")
    private Integer imgNo;

    @Column(name = "prod_no")
    private Integer prodNo;

    @Column(name = "upload_date")
    private Date uploadDate;

    @Column(name = "up_file", columnDefinition = "MEDIUMBLOB")
    private byte[] upFile;
}
