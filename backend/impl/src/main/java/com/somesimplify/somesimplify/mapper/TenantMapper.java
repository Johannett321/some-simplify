package com.somesimplify.somesimplify.mapper;

import com.somesimplify.model.TenantTO;
import com.somesimplify.somesimplify.model.Tenant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TenantMapper {

    @Mapping(target = "id", ignore = true)
    Tenant updateTenantFromTO(TenantTO tenantTO, @MappingTarget Tenant tenant);

    TenantTO toTenantTO(Tenant tenant);
}
