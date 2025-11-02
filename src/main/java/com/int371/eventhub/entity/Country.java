package com.int371.eventhub.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "COUNTRYS")
public class Country {
    @Id
    private Integer id;

    @Column(name = "NAME")
    private String countryName;

    @Column(name = "DESCRIPTION")
    private String countryDescription;
}
