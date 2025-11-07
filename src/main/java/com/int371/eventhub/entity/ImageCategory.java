package com.int371.eventhub.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "IMAGES_CATEGORY")
public class ImageCategory {

    @Id
    @Column(name = "ID")
    private Integer id;

    @Column(name = "NAME") 
    private String categoryName;

    @Column(name = "DESCRIPTION") 
    private String categoryDescription;
}