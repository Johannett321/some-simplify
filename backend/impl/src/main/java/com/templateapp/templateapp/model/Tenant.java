package com.templateapp.templateapp.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
public class Tenant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String name;

    private String orgNumber;
    private String email;
    private String phone;
    private String address1;
    private String address2;
    private String postalCode;
    private String city;
    private String logoUrl;
    private boolean vatRegistered;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @OneToMany(cascade = CascadeType.ALL)
    private List<User> users;
}
