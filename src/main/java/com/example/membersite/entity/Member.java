package com.example.membersite.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "members")
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String loginId;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 30)
    private String name;

    protected Member() {
    }

    public Member(String loginId, String password, String name) {
        this.loginId = loginId;
        this.password = password;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getLoginId() {
        return loginId;
    }

    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }

    public void changeName(String name) {
        this.name = name;
    }

    public void changePassword(String password) {
        this.password = password;
    }
}
