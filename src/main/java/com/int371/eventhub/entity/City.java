package com.int371.eventhub.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "CITIES")
public class City {
    @Id
    private Integer id;

    @Column(name = "NAME_TH")
    private String cityNameTh;

    @Column(name = "NAME_EN")
    private String cityNameEn;
    
    @Column(name = "COUNTRY_ID")
    private Integer countryId;
}
