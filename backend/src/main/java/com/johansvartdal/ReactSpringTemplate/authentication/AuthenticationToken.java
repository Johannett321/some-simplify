package com.johansvartdal.ReactSpringTemplate.authentication;

import org.springframework.security.authentication.AbstractAuthenticationToken;

public class AuthenticationToken extends AbstractAuthenticationToken {

    private final String sessionToken;
    private final String principal;

    public AuthenticationToken(String sessionToken) {
        super(null);
        this.sessionToken = sessionToken;
        this.principal = Authenticate.getClerkUserID(sessionToken);
        super.setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return sessionToken;
    }

    /**
     * This returns the principal (ClerkID) of the user
     * @return ClerkID
     */
    @Override
    public String getPrincipal() {
        return principal; // no principal in this case
    }
}
