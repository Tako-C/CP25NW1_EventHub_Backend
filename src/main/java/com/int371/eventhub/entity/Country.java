package com.int371.eventhub.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "COUNTRIES")
public class Country {
    @Id
    private Integer id;

    @Column(name = "NAME_TH")
    private String countryNameTh;

    @Column(name = "NAME_EN")
    private String countryNameEn;
}
