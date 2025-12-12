package com.somesimplify.somesimplify.mapper;

import com.somesimplify.model.ImageTO;
import com.somesimplify.somesimplify.model.Image;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Mapper(componentModel = "spring")
public interface ImageMapper {

    @Mapping(target = "uploadedBy", source = "uploadedBy.id")
    @Mapping(target = "url", ignore = true)
    ImageTO toImageTO(Image image);

    default OffsetDateTime map(LocalDateTime value) {
        return value != null ? value.atOffset(ZoneOffset.UTC) : null;
    }
}
