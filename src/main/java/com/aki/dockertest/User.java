package com.aki.dockertest;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer userId;
    @Column
    private String userName;
    @Column
    private Integer userAge;
    @Column
    private String userAddress;
}
