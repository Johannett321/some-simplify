package com.templateapp.templateapp.mapper;

import com.templateapp.model.CreateUserCommand;
import com.templateapp.model.UserTO;
import com.templateapp.templateapp.model.User;
import com.templateapp.templateapp.utils.FormatUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Locale;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserTO toUserResponseTO(User user);

    @Mapping(target = "email", qualifiedByName = "lowerCase")
    @Mapping(target = "firstName", qualifiedByName = "camelCase")
    @Mapping(target = "lastName", qualifiedByName = "camelCase")
    @Mapping(target = "password", qualifiedByName = "encodePassword")
    User toEntity(CreateUserCommand registerUserCommandTO);

    // Custom mapping method for LocalDateTime -> OffsetDateTime
    default OffsetDateTime map(LocalDateTime value) {
        return value != null ? value.atOffset(ZoneOffset.UTC) : null;
    }

    @Named("lowerCase")
    default String lowerCase(String value) {
        return value == null ? null : value.toLowerCase(Locale.ROOT);
    }

    @Named("camelCase")
    static String camelCase(String str) {
        return FormatUtils.convertToCamelCase(str);
    }

    @Named("encodePassword")
    default String encodePassword(String password) {
        if (password == null) return null;
        return new BCryptPasswordEncoder().encode(password);
    }
}
