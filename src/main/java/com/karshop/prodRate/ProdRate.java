package com.karshop.prodRate;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;



@Data
@Entity
@Table(name = "prod_rate")
public class ProdRate {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "prod_rate_no", updatable = false)
	private Integer prodRateNo;

	@Column(name = "ord_no")
	@NotNull(message = "訂單編號缺失")
	private Integer ordNo;

	@Column(name = "member_no")
	@NotNull(message = "會員編號缺失")
	private Integer memberNo;

	@Column(name = "prod_no")
	@NotNull(message = "商品編號缺失")
	private Integer prodNo;

	@Column(name = "rate")
	@NotNull(message = "請選擇評分星級")
	@Min(value = 1, message = "請至少給予 1 星評分")
	@Max(value = 5, message = "評分最高為 5 星")
	private Integer rate;

	@Column(name = "rate_content")
	@NotBlank(message = "請填寫評價內容") // 修改處：使用 @NotBlank 確保字串不為空且不含空白
	@Size(max = 800, message = "評價內容不能超過800字")
	private String rateContent;

	@Column(name = "rate_pic", columnDefinition = "mediumblob")
	private byte[] ratePic;

	@Column(name = "rate_time", updatable = false)
	private LocalDateTime rateTime;

	@Column(name = "rate_status")
	// 1: 已評價一次（已填寫，可再編輯一次）
	// 2: 已評價二次（已編輯過，鎖定不可再編輯）
	private Integer rateStatus;
}


