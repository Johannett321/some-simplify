package com.somesimplify.somesimplify.mapper;

import com.somesimplify.model.SocialMediaConnectionTO;
import com.somesimplify.somesimplify.model.SocialMediaConnection;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SocialMediaConnectionMapper {

    SocialMediaConnectionTO toTO(SocialMediaConnection connection);
}
