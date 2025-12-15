package com.somesimplify.somesimplify.model;

import com.somesimplify.model.PlatformType;
import com.somesimplify.model.PostStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@Entity
@EqualsAndHashCode(callSuper = true)
public class Post extends AbstractBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private OffsetDateTime publishAt;

    @Column(columnDefinition = "text")
    private String text;

    @Enumerated(EnumType.STRING)
    private List<PlatformType> platforms;

    @OneToMany
    private List<ContentFile> contentFiles;

    @Enumerated(EnumType.STRING)
    private PostStatus status;
}
