// CustomOAuth2UserService.java
package com.retrouvtout.security.oauth2;

import com.retrouvtout.entity.OAuthAccount;
import com.retrouvtout.entity.User;
import com.retrouvtout.exception.OAuth2AuthenticationProcessingException;
import com.retrouvtout.repository.OAuthAccountRepository;
import com.retrouvtout.repository.UserRepository;
import com.retrouvtout.security.UserPrincipal;
import com.retrouvtout.security.oauth2.user.OAuth2UserInfo;
import com.retrouvtout.security.oauth2.user.OAuth2UserInfoFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Optional;

/**
 * Service personnalisé pour l'authentification OAuth2
 */
@Service
@Transactional
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OAuthAccountRepository oAuthAccountRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(oAuth2UserRequest);

        try {
            return processOAuth2User(oAuth2UserRequest, oAuth2User);
        } catch (AuthenticationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex.getCause());
        }
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User) {
        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(
                oAuth2UserRequest.getClientRegistration().getRegistrationId(), 
                oAuth2User.getAttributes()
        );
        
        if (!StringUtils.hasText(oAuth2UserInfo.getEmail())) {
            throw new OAuth2AuthenticationProcessingException("Email non trouvé dans la réponse OAuth2");
        }

        Optional<User> userOptional = userRepository.findByEmailAndActiveTrue(oAuth2UserInfo.getEmail());
        User user;
        
        if (userOptional.isPresent()) {
            user = userOptional.get();
            user = updateExistingUser(user, oAuth2UserInfo);
        } else {
            user = registerNewUser(oAuth2UserRequest, oAuth2UserInfo);
        }

        // Créer un UserPrincipal qui implémente OAuth2User
        return new OAuth2UserPrincipal(UserPrincipal.create(user), oAuth2User.getAttributes());
    }

    private User registerNewUser(OAuth2UserRequest oAuth2UserRequest, OAuth2UserInfo oAuth2UserInfo) {
        User user = new User();
        user.setId(java.util.UUID.randomUUID().toString());
        user.setName(oAuth2UserInfo.getName());
        user.setEmail(oAuth2UserInfo.getEmail());
        user.setEmailVerified(true); // OAuth2 emails are pre-verified
        user.setRole(User.UserRole.MIXTE);
        user.setActive(true);
        
        user = userRepository.save(user);

        // Créer le compte OAuth2
        OAuthAccount oAuthAccount = new OAuthAccount();
        oAuthAccount.setUser(user);
        oAuthAccount.setProvider(oAuth2UserRequest.getClientRegistration().getRegistrationId());
        oAuthAccount.setProviderUserId(oAuth2UserInfo.getId());
        
        oAuthAccountRepository.save(oAuthAccount);

        return user;
    }

    private User updateExistingUser(User existingUser, OAuth2UserInfo oAuth2UserInfo) {
        existingUser.setName(oAuth2UserInfo.getName());
        return userRepository.save(existingUser);
    }

    /**
     * Wrapper pour UserPrincipal qui implémente OAuth2User
     */
    public static class OAuth2UserPrincipal extends UserPrincipal implements OAuth2User {
        private java.util.Map<String, Object> attributes;

        public OAuth2UserPrincipal(UserPrincipal userPrincipal, java.util.Map<String, Object> attributes) {
            super(userPrincipal.getId(), userPrincipal.getName(), userPrincipal.getEmail(), 
                  userPrincipal.getPassword(), userPrincipal.getAuthorities(), 
                  userPrincipal.isEnabled(), userPrincipal.isEmailVerified());
            this.attributes = attributes;
        }

        @Override
        public java.util.Map<String, Object> getAttributes() {
            return attributes;
        }

        @Override
        public String getName() {
            return super.getName();
        }
    }
}