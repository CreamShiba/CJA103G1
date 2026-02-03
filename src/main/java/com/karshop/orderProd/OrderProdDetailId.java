package com.karshop.orderProd;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderProdDetailId implements Serializable {
    private Integer ordNo;
    private Integer prodNo;
}