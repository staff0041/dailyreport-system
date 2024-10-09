package com.techacademy.entity;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.SQLRestriction;
import org.hibernate.validator.constraints.Length;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
// import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@Entity
@Table(name = "employees")
@SQLRestriction("delete_flg = false")

public class Employee {
    // 社員番号
    @Id
    @Column(length = 10) //登録時の制約
    @NotEmpty
    @Length(max = 10   ) //入力時の制約
    // @Pattern(regexp = "^[a-zA-Z0-9]+$")
    private String code;

    // 氏名
    @Column(length = 20, nullable = false)
    @NotEmpty
    @Length(max = 20)
    private String name;

    // 権限
    @Column(columnDefinition="VARCHAR(10)", nullable = false)
    @Enumerated(EnumType.STRING) //列挙
    private Role role;

    public static enum Role {
        GENERAL("一般"),
        ADMIN("管理者");

        private String name;
        private Role(String name) {
            this.name = name;
        }
        public String getValue() {
            return this.name;
        }
    }

    // パスワード
    @Column(length = 255, nullable = false)
    private String password;

    // 削除フラグ(論理削除)
    @Column(columnDefinition="TINYINT", nullable = false)
    private boolean deleteFlg;

    // 登録日時
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 更新日時
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.deleteFlg = false;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // リレーション
    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL)
    private List<Report> reportList;
}
