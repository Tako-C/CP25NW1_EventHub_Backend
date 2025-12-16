package com.int371.eventhub.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "JOBS")
public class Job {
    @Id
    private Integer id;

    @Column(name = "NAME_TH")
    private String jobNameTh;

    @Column(name = "NAME_EN")
    private String jobNameEn;
}
