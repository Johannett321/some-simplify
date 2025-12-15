package com.somesimplify.somesimplify.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@EqualsAndHashCode(callSuper = true)
public class ContentFile extends AbstractBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false, unique = true)
    private String s3Key;

    @Column(nullable = false, unique = true)
    private String thumbnailS3Key;

    @Column(nullable = false)
    private String contentType;

    @Column(nullable = false)
    private Long fileSize;

    @ManyToOne(fetch = FetchType.LAZY)
    private User uploadedBy;
}
