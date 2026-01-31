package com.karshop.install.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "install_order_detail")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InstallOrderDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "detail_no")
    private Integer detailNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "install_order_no", nullable = false)
    private InstallOrder installOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ts_no", nullable = false)
    private TechnicianService technicianService;

    @Column(name = "price", nullable = false)
    private Integer price;
}
