package com.somesimplify.somesimplify.dto;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class CustomOauth2User implements OAuth2User, Serializable {

    private final OAuth2User oAuth2User;

    @Setter
    private Collection<? extends GrantedAuthority> authorities;

    @Override
    public Map<String, Object> getAttributes() {
        return oAuth2User.getAttributes();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getName() {
        return oAuth2User.getAttribute("name");
    }

    public String getFacebookId() {
        return oAuth2User.getAttribute("id");
    }

    public String getGoogleSub() {
        return oAuth2User.getAttribute("sub");
    }

    public String getEmail() {
        return oAuth2User.getAttribute("email");
    }

    public String getFirstName() {
        String firstName = oAuth2User.getAttribute("given_name");
        if (firstName == null || firstName.isEmpty()) {
            firstName = oAuth2User.getAttribute("first_name");
        }

        if (firstName == null || firstName.isEmpty()) {
            String fullName =  oAuth2User.getAttribute("name");
            if (fullName != null && !fullName.isEmpty()) {
                firstName = fullName.split(" ")[0];
            }
        }
        return firstName;
    }

    public String getLastName() {
        String lastName = oAuth2User.getAttribute("family_name");
        if (lastName == null || lastName.isEmpty()) {
            lastName = oAuth2User.getAttribute("last_name");
        }

        if (lastName == null || lastName.isEmpty()) {
            String fullName = oAuth2User.getAttribute("name");
            if (fullName != null && !fullName.isEmpty()) {
                String[] names = fullName.split(" ");
                if (names.length > 1) {
                    lastName = names[names.length-1];
                }
            }
        }

        return lastName;
    }
}
