package com.somesimplify.somesimplify.mapper;

import com.somesimplify.model.PostTO;
import com.somesimplify.somesimplify.model.Post;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {ImageMapper.class})
public interface PostMapper {
    PostTO toPostTO(Post post);

    @Mapping(target = "contentFiles", ignore = true)
    Post toPost(PostTO postTO);
}
