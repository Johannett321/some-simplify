package com.somesimplify.somesimplify.dto.instagram;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class InstagramAccountInfo {

    private String id;

    private String username;

    @JsonProperty("account_type")
    private String accountType;
}
