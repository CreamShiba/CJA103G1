package com.karshop.model.entity; // 確認你的 package 名稱是否正確

import jakarta.persistence.*;
import java.util.Set;

@Entity
@Table(name = "categories")
public class Categories {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "category_id")
	private Integer categoryId;

	@Column(name = "name", nullable = false, unique = true)
	private String name;

	@Column(name = "description", columnDefinition = "TEXT")
	private String description;

	// 這裡加上 mappedBy 是為了雙向關聯（可選），方便之後查詢「某分類下所有文章」
	@OneToMany(mappedBy = "categories", cascade = CascadeType.ALL)
	private Set<ForumPost> forumPosts;

	public Categories() {
	}

	public Categories(String name, String description) {
		this.name = name;
		this.description = description;
	}

	// Getters and Setters
	public Integer getCategoryId() { return categoryId; }
	public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }

	public String getName() { return name; }
	public void setName(String name) { this.name = name; }

	public String getDescription() { return description; }
	public void setDescription(String description) { this.description = description; }
}