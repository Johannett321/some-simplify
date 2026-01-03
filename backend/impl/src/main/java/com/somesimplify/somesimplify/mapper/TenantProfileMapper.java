package com.somesimplify.somesimplify.mapper;

import com.somesimplify.model.TenantProfileTO;
import com.somesimplify.somesimplify.model.TenantProfile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Mapper(componentModel = "spring")
public interface TenantProfileMapper {

    @Mapping(target = "tenantId", source = "tenant.id")
    TenantProfileTO toTO(TenantProfile profile);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tenant", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    TenantProfile updateFromTO(TenantProfileTO profileTO, @MappingTarget TenantProfile profile);

    // Custom mapping methods for type conversions
    default URI stringToUri(String url) {
        if (url == null) {
            return null;
        }
        try {
            return URI.create(url);
        } catch (Exception e) {
            return null;
        }
    }

    default String uriToString(URI uri) {
        return uri != null ? uri.toString() : null;
    }

    default OffsetDateTime localDateTimeToOffsetDateTime(LocalDateTime localDateTime) {
        return localDateTime != null ? localDateTime.atOffset(ZoneOffset.UTC) : null;
    }

    default LocalDateTime offsetDateTimeToLocalDateTime(OffsetDateTime offsetDateTime) {
        return offsetDateTime != null ? offsetDateTime.toLocalDateTime() : null;
    }
}
