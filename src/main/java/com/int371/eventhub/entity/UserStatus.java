package com.int371.eventhub.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "USER_STATUS")
public class UserStatus {
    @Id
    private Integer id;

    @Column(name = "NAME")
    private String statusName;

    @Column(name = "DESCRIPTION")
    private String statusDescription;
}
