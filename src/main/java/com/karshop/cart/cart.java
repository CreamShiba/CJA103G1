package com.karshop.cart;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "cart")
@IdClass(cart.cartId.class)  // ⭐ 加上這個，告訴JPA這是複合主鍵
public class cart {

    // 主鍵，會員編號
    @Id
    @Column(name = "member_no", nullable = false)
    private Integer member_no;

    // 主鍵，商品編號
    @Id
    @Column(name = "prod_no", nullable = false)
    private Integer prod_no;

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "added_time")
    private LocalDateTime added_time;

    // 新增：商品數量
    @Column(name = "quantity")
    private Integer quantity = 1;

    // 建構子
    public cart() {
    }

    public cart(Integer member_no, Integer prod_no) {
        this.member_no = member_no;
        this.prod_no = prod_no;
        this.added_time = LocalDateTime.now();
    }

    public Integer getMember_no() {
        return member_no;
    }

    public void setMember_no(Integer member_no) {
        this.member_no = member_no;
    }

    public Integer getProd_no() {
        return prod_no;
    }

    public void setProd_no(Integer prod_no) {
        this.prod_no = prod_no;
    }

    public LocalDateTime getAdded_time() {
        return added_time;
    }

    public void setAdded_time(LocalDateTime added_time) {
        this.added_time = added_time;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    // ⭐ 複合主鍵類別（一定要加這個！）
    public static class cartId implements Serializable {

        private Integer member_no;
        private Integer prod_no;

        public cartId() {
        }

        public cartId(Integer member_no, Integer prod_no) {
            this.member_no = member_no;
            this.prod_no = prod_no;
        }

        public Integer getMember_no() {
            return member_no;
        }

        public void setMember_no(Integer member_no) {
            this.member_no = member_no;
        }

        public Integer getProd_no() {
            return prod_no;
        }

        public void setProd_no(Integer prod_no) {
            this.prod_no = prod_no;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            cartId cartId = (cartId) o;
            return Objects.equals(member_no, cartId.member_no) &&
                    Objects.equals(prod_no, cartId.prod_no);
        }

        @Override
        public int hashCode() {
            return Objects.hash(member_no, prod_no);
        }
    }
}