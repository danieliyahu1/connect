//package com.akatsuki.auth.component;
//
//import com.akatsuki.auth.configuration.ComponentTestSecurityConfig;
//import com.akatsuki.auth.dto.OAuthResponseDTO;
//import com.akatsuki.auth.enums.AuthProvider;
//import com.akatsuki.auth.model.RefreshToken;
//import com.akatsuki.auth.model.User;
//import com.akatsuki.auth.repository.AuthRepository;
//import com.akatsuki.auth.repository.UserRepository;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.junit.jupiter.api.Test;
//import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
//import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
//import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
//import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.context.annotation.Import;
//import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.context.bean.override.mockito.MockitoBean;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.MvcResult;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//import java.util.Optional;
//
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@EnableAutoConfiguration(exclude = {
//        DataSourceAutoConfiguration.class,
//        JpaRepositoriesAutoConfiguration.class,
//        HibernateJpaAutoConfiguration.class
//})
//@Import(ComponentTestSecurityConfig.class)
//@ActiveProfiles("component-test")
//@AutoConfigureMockMvc
//    public class OAuthServiceComponentTest {
//
//        @Autowired
//        private MockMvc mockMvc;
//
//        @Autowired
//        private ObjectMapper objectMapper;
//
//        @MockitoBean
//        private UserRepository userRepository;
//
//        @MockitoBean
//        private AuthRepository authRepository;
//
//        @Test
//        void oauth2LoginCallback_newUser_shouldReturn201AndSaveRefreshToken() throws Exception {
//            // Simulate user not found → new user registration
//            when(userRepository.findByEmail(any())).thenReturn(Optional.empty());
//            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
//            when(authRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));
//
//            MvcResult result = mockMvc.perform(get("/login/oauth2/code/google")
//                            .with(oauth2Login()
//                                    .attributes(attrs -> {
//                                        attrs.put("email", "daniel@example.com");
//                                        attrs.put("sub", "google");})
//                            ))
//                    .andExpect(status().isCreated())
//                    .andReturn();
//
//            String jsonResponse = result.getResponse().getContentAsString();
//            OAuthResponseDTO responseDTO = objectMapper.readValue(jsonResponse, OAuthResponseDTO.class);
//
//            assert responseDTO.getAccessToken() != null;
//            assert responseDTO.getRefreshToken() != null;
//            assert responseDTO.isNewUser();
//
//            verify(authRepository).save(any(RefreshToken.class));
//        }
//
//        @Test
//        void oauth2LoginCallback_existingUser_shouldReturn200() throws Exception {
//            // Simulate existing user found
//            User existingUser = new User("test@example.com", AuthProvider.GOOGLE, "123id"); // fill with realistic data as needed
//            when(userRepository.findByEmail(any())).thenReturn(Optional.of(existingUser));
//
//            when(authRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));
//
//            MvcResult result = mockMvc.perform(get("/login/oauth2/code/google")
//                            .with(SecurityMockMvcRequestPostProcessors.oauth2Login()))
//                    .andExpect(status().isOk()) // HTTP 200 for existing user
//                    .andReturn();
//
//            String jsonResponse = result.getResponse().getContentAsString();
//            OAuthResponseDTO responseDTO = objectMapper.readValue(jsonResponse, OAuthResponseDTO.class);
//
//            assert responseDTO.getAccessToken() != null;
//            assert responseDTO.getRefreshToken() != null;
//            assert !responseDTO.isNewUser();
//
//            verify(authRepository).save(any(RefreshToken.class));
//        }
//    }
