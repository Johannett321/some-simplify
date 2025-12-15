package com.somesimplify.somesimplify.mapper;

import com.somesimplify.model.ImageTO;
import com.somesimplify.somesimplify.model.ContentFile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Mapper(componentModel = "spring")
public interface ImageMapper {

    @Mapping(target = "uploadedBy", source = "uploadedBy.id")
    @Mapping(target = "url", ignore = true)
    @Mapping(target = "thumbnailUrl", ignore = true)
    ImageTO toImageTO(ContentFile contentFile);

    default OffsetDateTime map(LocalDateTime value) {
        return value != null ? value.atOffset(ZoneOffset.UTC) : null;
    }
}
