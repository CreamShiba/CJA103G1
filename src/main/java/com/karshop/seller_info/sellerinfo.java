package com.karshop.seller_info;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "seller_info")
public class sellerinfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seller_no", nullable = false)
    private Integer seller_no;

    @Column(name = "member_no")
    private Integer member_no;

    @Column(name = "shop_name")
    private String shop_name;

    @Column(name = "seller_name")
    private String seller_name;

    @Column(name = "phone")
    private String phone;
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "email")
    private String email;
    
    @Column(name = "address")
    private String address;
    
    @Column(name = "status")
    private String status;

    @Column(name = "isverified", nullable = false, columnDefinition = "TINYINT")
    private Boolean isverified = false; 
    
    @Column(name = "verifies")
    private LocalDateTime verifies;

    @Column(name = "created")
    private LocalDateTime created;

    @Column(name = "updated")
    private LocalDateTime updated;
    
    @Column(name = "rating_amount")
    private Integer rating_amount = 0;
    
    @Column(name = "rating_star")
    private Integer rating_star = 0;
    
    @Column(name = "image_path")
    private String image_path;

	@Column(name = "bank_name")
	private String bank_name;

	@Column(name = "bank_code")
	private String bank_code;

	@Column(name = "bank_account")
	private String bank_account;

	@Column(name = "account_holder")
	private String account_holder;

	@Column(name = "seller_tax_id", length = 20)
	private String seller_tax_id;

	public Integer getSeller_no() {
		return seller_no;
	}

	public void setSeller_no(Integer seller_no) {
		this.seller_no = seller_no;
	}

	public Integer getMember_no() {
		return member_no;
	}

	public void setMember_no(Integer member_no) {
		this.member_no = member_no;
	}

	public String getShop_name() {
		return shop_name;
	}

	public void setShop_name(String shop_name) {
		this.shop_name = shop_name;
	}

	public String getSeller_name() {
		return seller_name;
	}

	public void setSeller_name(String seller_name) {
		this.seller_name = seller_name;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Boolean getIsverified() {
	    return isverified;
	}

	public void setIsverified(Boolean isverified) {
	    this.isverified = isverified;
	}

	public LocalDateTime getVerifies() {
		return verifies;
	}

	public void setVerifies(LocalDateTime verifies) {
		this.verifies = verifies;
	}

	public LocalDateTime getCreated() {
		return created;
	}

	public void setCreated(LocalDateTime created) {
		this.created = created;
	}

	public LocalDateTime getUpdated() {
		return updated;
	}

	public void setUpdated(LocalDateTime updated) {
		this.updated = updated;
	}

	public Integer getRating_amount() {
		return rating_amount;
	}

	public void setRating_amount(Integer rating_amount) {
		this.rating_amount = rating_amount;
	}

	public Integer getRating_star() {
		return rating_star;
	}

	public void setRating_star(Integer rating_star) {
		this.rating_star = rating_star;
	}

	public String getImage_path() {
		return image_path;
	}

	public void setImage_path(String image_path) {
		this.image_path = image_path;
	}

	public String getBank_name() {
		return bank_name;
	}

	public void setBank_name(String bank_name) {
		this.bank_name = bank_name;
	}

	public String getBank_code() {
		return bank_code;
	}

	public void setBank_code(String bank_code) {
		this.bank_code = bank_code;
	}

	public String getBank_account() {
		return bank_account;
	}

	public void setBank_account(String bank_account) {
		this.bank_account = bank_account;
	}

	public String getAccount_holder() {
		return account_holder;
	}

	public void setAccount_holder(String account_holder) {
		this.account_holder = account_holder;
	}

	public String getSeller_tax_id() {
		return seller_tax_id;
	}

	public void setSeller_tax_id(String seller_tax_id) {
		this.seller_tax_id = seller_tax_id;
	}

//	@Override
//	public String toString() {
//		return "SellerInfo [seller_no=" + seller_no + ", member_no=" + member_no + ", shop_name=" + shop_name + ", seller_name=" + seller_name +
//							", phone=" + phone+ ", description=" + description + ", email=" + email + ", address=" + address +
//							", status=" + status + ", isverified=" + isverified + ", verifies=" + verifies + ", created=" + created +
//							", updated=" + updated + ", rating_amount=" + rating_amount + ", rating_star=" + rating_star + ", image_path=" + image_path +
//				            ", bank_name=" +bank_name + ", bank_code=" + bank_code + ", bank_account=" + bank_account + ", account_holder=" + account_holder + "]";
//	}
}