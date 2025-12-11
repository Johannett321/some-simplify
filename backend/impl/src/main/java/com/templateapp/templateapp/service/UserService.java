package com.templateapp.templateapp.service;

import com.templateapp.model.CreateUserCommand;
import com.templateapp.model.LoginUserCommand;
import com.templateapp.templateapp.config.ApplicationConfig;
import com.templateapp.templateapp.dto.CustomOauth2User;
import com.templateapp.templateapp.exception.ConflictException;
import com.templateapp.templateapp.exception.ResourceNotFoundException;
import com.templateapp.templateapp.exception.UnauthorizedException;
import com.templateapp.templateapp.mapper.UserMapper;
import com.templateapp.templateapp.model.ApplicationRole;
import com.templateapp.templateapp.model.User;
import com.templateapp.templateapp.repository.ApplicationRoleRepository;
import com.templateapp.templateapp.repository.UserRepository;
import com.templateapp.templateapp.utils.JwtUtil;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final ApplicationConfig applicationConfig;
    private final UserRepository userRepository;
    private final ApplicationRoleRepository applicationRoleRepository;
    private final AuthenticationManager authenticationManager;
    private final HttpServletRequest httpServletRequest;
    private final CustomUserDetailsService customUserDetailsService;
    private final JwtUtil jwtUtil;
    private final HttpServletResponse httpServletResponse;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(ResourceNotFoundException::new);
    }

    /**
     * Returns the user that is currently logged in
     *
     * @return The user that is currently logged in
     */
    public User getCurrentUser() {
        Authentication token = SecurityContextHolder.getContext().getAuthentication();

        CustomOauth2User principal = null;
        try {
            principal = (CustomOauth2User) token.getPrincipal();
        } catch (Exception ignored) {}

        User currentUser;
        if (principal != null) {
            currentUser = getUserByEmail(principal.getEmail());
        } else {
            currentUser = getUserByEmail(token.getName());
        }

        return currentUser;
    }

    /**
     * Creates the user if it does not exist. It is based on the currently logged in user.
     */
    public void createUserIfItDoesNotExist(CustomOauth2User oAuth2User, Map<String, Object> utmData) {
        Optional<User> optionalUser = findUserByOAuthId(oAuth2User);

        if (optionalUser.isPresent()) {
            updateEmailIfNecessary(optionalUser.get(), oAuth2User);
            return;
        }

        // make sure the user has not already registered without oAuth
        ensureEmailNotTaken(oAuth2User.getEmail().toLowerCase());

        // create the user
        createUserWithOAuth(oAuth2User, utmData);
    }

    /**
     * Returns the user by searching for it's oAuth Id
     * @param oAuth2User The oAuth2 user to search for
     * @return The user if found. Optional.empty() otherwise.
     */
    private Optional<User> findUserByOAuthId(CustomOauth2User oAuth2User) {
        if (hasFacebookId(oAuth2User)) {
            return userRepository.findByFacebookId(oAuth2User.getFacebookId());
        } else if (hasGoogleSub(oAuth2User)) {
            return userRepository.findByGoogleSub(oAuth2User.getGoogleSub());
        }
        return Optional.empty();
    }


    /**
     * Checks the oAuth2 object if it contains a Facebook ID (like all Facebook signed in users do)
     */
    private boolean hasFacebookId(CustomOauth2User oAuth2User) {
        return oAuth2User.getFacebookId() != null && !oAuth2User.getFacebookId().isEmpty();
    }

    /**
     * Checks the oAuth2 object if it contains a Google SUB (ID) (like all Google signed in users do)
     */
    private boolean hasGoogleSub(CustomOauth2User oAuth2User) {
        return oAuth2User.getGoogleSub() != null && !oAuth2User.getGoogleSub().isEmpty();
    }

    /**
     * Updates the email of a user if it has changed based on the oAuth2 object that the user used to sign in
     */
    private void updateEmailIfNecessary(User user, CustomOauth2User oAuth2User) {
        String newEmail = oAuth2User.getEmail();
        if (newEmail != null && !newEmail.isEmpty() && !user.getEmail().equals(newEmail)) {
            String oldEmail = user.getEmail();
            user.setEmail(newEmail.toLowerCase());
            userRepository.save(user);
            log.info("Email of user {} was changed from {} to {}. Updating database!",
                    oAuth2User.getFirstName(), oldEmail, newEmail);
        }
    }

    /**
     * Throws a conflict exception if the email already exists in the database without oAuth
     */
    private void ensureEmailNotTaken(String email) {
        Optional<User> optionalUserByEmail = userRepository.findByEmail(email);
        if (optionalUserByEmail.isPresent()) {
            throw new ConflictException("User with email " + email +
                    " already exists. Please sign in the same way you created your account.");
        }
    }

    /**
     * Creates an Enthemed user based on an oAuth principal
     * @param oAuth2User The principal to create the user from
     */
    private void createUserWithOAuth(CustomOauth2User oAuth2User, Map<String, Object> utmData) {
        User user = new User();
        user.setEmail(oAuth2User.getEmail().toLowerCase());
        user.getApplicationRoles().add(applicationRoleRepository.findByName("USER").orElseThrow());
        user.setFacebookId(oAuth2User.getFacebookId());
        user.setGoogleSub(oAuth2User.getGoogleSub());
        user.setFirstName(oAuth2User.getFirstName());
        user.setLastName(oAuth2User.getLastName());
    }

    /**
     * Attempts to register the user
     *
     * @param createUserCommand The DTO containing the registration information
     */
    public void createUserWithBasicAuth(CreateUserCommand createUserCommand) {
        //make sure the following method throws an error:
        try {
            getUserByEmail(createUserCommand.getEmail());
            throw new ConflictException("Email is already in use");
        } catch (ResourceNotFoundException ignore) {}

        // make sure the password is valid
        if (!passwordIsValid(createUserCommand.getPassword())) {
            throw new UnauthorizedException("Password is not valid");
        }

        // create the user
        User user = userMapper.toEntity(createUserCommand);
        if (user.getApplicationRoles() == null) {
            user.setApplicationRoles(new HashSet<>());
        }
        user.getApplicationRoles().add(applicationRoleRepository.findByName("USER").orElseThrow());

        user = userRepository.save(user);

        log.info("Created user with email: {}", createUserCommand.getEmail());

        authenticateUser(user, createUserCommand.getPassword());
    }

    private void authenticateUser(User user, String rawPassword) {
        // Manually authenticate the user after successful registration
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user.getEmail(),
                rawPassword
        );

        // Use the authentication manager to authenticate the user
        Authentication auth = authenticationManager.authenticate(authentication);

        // Set the authentication to the security context
        SecurityContextHolder.getContext().setAuthentication(auth);

        // Create a new session for the user
        HttpSession session = httpServletRequest.getSession(true);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());

        final UserDetails userDetails = customUserDetailsService.fromUser(user);

        final String token = jwtUtil.generateToken(userDetails);

        ResponseCookie cookie = ResponseCookie.from("token", token)
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ofDays(JwtUtil.getExpirationDays()))
                .build();

        httpServletResponse.addHeader("Set-Cookie", cookie.toString());
    }

    /**
     * Sends an email to the user with a link to reset the password
     *
     * @param resetPasswordDTO The DTO containing the email of the user
     */
/*
    public void resetPassword(ResetPasswordDTO resetPasswordDTO) {
        User user = getUserByEmail(resetPasswordDTO.getEmail());
        ResetPasswordRequest resetPasswordRequest = new ResetPasswordRequest();
        resetPasswordRequest.setEmail(user.getEmail().toLowerCase());
        resetPasswordRequest.setExpiration(System.currentTimeMillis() + 1000 * 60 * 20);
        resetPasswordRepository.save(resetPasswordRequest);

        Email email = new Email();
        email.setTo(user.getEmail());
        email.setSubject("Reset password");
        email.setBody("Click here to reset your password: " + getFrontendUrl() + "change-password?token=" + resetPasswordRequest.getId());
        try {
            emailService.sendEmail(email);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }
 */

    /**
     * Updates the password for the user
     *
     * @param updatePasswordDTO The DTO containing the new password and the token
     */
    /*
    public void updatePassword(UpdatePasswordDTO updatePasswordDTO) {
        // check if token exists
        ResetPasswordRequest request = resetPasswordRepository.findById(updatePasswordDTO.getToken()).orElseThrow(ResourceNotFoundException::new);

        // check if token is expired
        if (System.currentTimeMillis() > request.getExpiration()) {
            throw new ResourceNotFoundException();
        }

        // make sure new password is valid
        if (!passwordIsValid(updatePasswordDTO.getPassword())) {
            throw new UnauthorizedException("Password is not valid");
        }

        // update password
        User user = getUserByEmail(request.getEmail());
        user.setPassword(new BCryptPasswordEncoder().encode(updatePasswordDTO.getPassword()));
        userRepository.save(user);

        // delete token
        resetPasswordRepository.delete(request);
    }
     */

    /**
     * Updates the user
     *
     * @param userUpdateDTO The DTO containing the new information for the user
     */
    /*
    public void updateUser(UpdateUserDTO userUpdateDTO, HttpServletResponse response) {
        User user = getCurrentUser();

        boolean emailUpdated = false;
        if (!user.getEmail().equalsIgnoreCase(userUpdateDTO.getEmail())) {
            ensureEmailNotTaken(userUpdateDTO.getEmail());
            user.setEmail(userUpdateDTO.getEmail().toLowerCase());
            emailUpdated = true;
        }

        user.setFirstName(userUpdateDTO.getFirstName());
        user.setLastName(userUpdateDTO.getLastName());

        if (userUpdateDTO.getNewPassword() != null || userUpdateDTO.getOldPassword() != null) {
            if (!new BCryptPasswordEncoder().matches(userUpdateDTO.getOldPassword(), user.getPassword())) {
                throw new UnauthorizedException("Old password is not correct");
            }
            if (!passwordIsValid(userUpdateDTO.getNewPassword())) {
                throw new BadRequestException("New password is not valid");
            }
            user.setPassword(new BCryptPasswordEncoder().encode(userUpdateDTO.getNewPassword()));
        }
        userRepository.save(user);

        // sign out user if email updated
        if (emailUpdated) {
            try {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication != null) {
                    SecurityContextHolder.clearContext();
                    ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
                    var session = attr.getRequest().getSession(false);
                    if (session != null) {
                        session.invalidate();
                    }
                    log.info("User session invalidated after email update for user: {}", user.getId());
                }
            }catch (Exception e) {
                log.error("Failed to invalidate session after email update for user: {}", user.getId(), e);
            }

            // expire login token
            ResponseCookie cookie = ResponseCookie.from("token", "")
                    .httpOnly(true)
                    .secure(true)
                    .sameSite("Lax")
                    .path("/")
                    .maxAge(0)
                    .build();
            response.addHeader("Set-Cookie", cookie.toString());
        }
    }
     */

    /**
     * Checks if the password is valid. NB: IT DOES NOT CHECK IF THE PASSWORD IS CORRECT FOR THE USER. ONLY IF IT IS VALID AND FOLLOWS THE RULES.
     *
     * @param password The password to check
     * @return True if the password is valid, false otherwise
     */
    public Boolean passwordIsValid(String password) {
        if (password == null || password.isEmpty()) {
            return false;
        }
        return password.length() >= 8 && password.length() <= 128 && password.matches(".*[a-z].*") && password.matches(".*[A-Z].*") && password.matches(".*[0-9].*");
    }

    public String createCustomerPortalLink() throws StripeException {
        Stripe.apiKey = stripeApiKey;
        User currentUser = getCurrentUser();

        if (currentUser.getStripeCustomerId() == null || currentUser.getStripeCustomerId().isEmpty()) {
            throw new RuntimeException(currentUser.getFirstName() + " " + currentUser.getLastName() + " attempted visiting customer portal, but failed due to missing Stripe customer ID");
        }

        com.stripe.param.billingportal.SessionCreateParams sessionCreateParams = com.stripe.param.billingportal.SessionCreateParams.builder()
                .setCustomer(currentUser.getStripeCustomerId())
                .setReturnUrl(applicationConfig.getFrontendUrl()).build();

        com.stripe.model.billingportal.Session session = com.stripe.model.billingportal.Session.create(sessionCreateParams);
        return session.getUrl();
    }

    @Bean
    CommandLineRunner init(ApplicationRoleRepository roleRepo) {
        return args -> {
            // Create roles if not present
            roleRepo.findByName("ADMIN").orElseGet(() -> roleRepo.save(new ApplicationRole(null, "ADMIN")));
            roleRepo.findByName("USER").orElseGet(() -> roleRepo.save(new ApplicationRole(null, "USER")));
        };
    }

    public void loginUser(LoginUserCommand loginCommand) {
        User user = getUserByEmail(loginCommand.getEmail().toLowerCase());
        if (user == null) {
            throw new BadCredentialsException("Invalid email or password");
        }
        if (!passwordEncoder.matches(loginCommand.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Wrong password");
        }
        authenticateUser(user);
    }

    /**
     * Assigns a JWT token to the user. NB! This method does not validate the password
     * @param user The user to that is related to the token
     */
    public void authenticateUser(User user) {
        // Build UserDetails (your JwtAuthenticationFilter will re-validate this on each request)
        final UserDetails userDetails = customUserDetailsService.fromUser(user);

        // Generate JWT
        final String token = jwtUtil.generateToken(userDetails);

        // Set JWT as HttpOnly cookie
        ResponseCookie cookie = ResponseCookie.from("token", token)
                .httpOnly(true)
                .secure(false) // ⚠️ for local dev; set true in prod (requires HTTPS)
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ofDays(JwtUtil.getExpirationDays()))
                .build();

        httpServletResponse.addHeader("Set-Cookie", cookie.toString());
    }

    public void logout() {
        // Set JWT as HttpOnly cookie
        ResponseCookie cookie = ResponseCookie.from("token", "")
                .httpOnly(true)
                .secure(false) // ⚠️ for local dev; set true in prod (requires HTTPS)
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ofMillis(1))
                .build();

        httpServletResponse.addHeader("Set-Cookie", cookie.toString());
    }
}
