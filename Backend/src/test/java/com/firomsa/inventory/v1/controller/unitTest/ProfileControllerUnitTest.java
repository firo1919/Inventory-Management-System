package com.firomsa.inventory.v1.controller.unitTest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.firomsa.inventory.model.Roles;
import com.firomsa.inventory.v1.controller.ProfileController;
import com.firomsa.inventory.v1.dto.ProfileUpdateDTO;
import com.firomsa.inventory.v1.dto.UserResponseDTO;
import com.firomsa.inventory.v1.service.UserService;

@WebMvcTest(ProfileController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ProfileControllerUnitTest {

    @MockitoBean
    private UserService userService;

    @Autowired
    private MockMvc mockMvc;

    private static final String BASE_URL = "/api/v1/profile";

    private UserResponseDTO sampleUser(UUID id, String username) {
        return new UserResponseDTO(id, "John", "Doe", username, username, "+251911111111",
                Roles.EMPLOYEE.name(), "https://cdn.example.com/profile.jpg", "2026-03-19T10:15:30",
                true, true);
    }

    @Test
    void shouldGetProfile() throws Exception {
        String email = "employee.one@example.com";
        UserResponseDTO response = sampleUser(UUID.randomUUID(), email);
        when(userService.getProfile(email)).thenReturn(response);

        mockMvc.perform(get(BASE_URL)
                .principal(new UsernamePasswordAuthenticationToken(email, "password123")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.role").value("EMPLOYEE"));

        verify(userService).getProfile(email);
    }

    @Test
    void shouldUpdateProfile() throws Exception {
        String email = "employee.one@example.com";
        UserResponseDTO response = sampleUser(UUID.randomUUID(), email);
        when(userService.updateProfile(eq(email), any(ProfileUpdateDTO.class)))
                .thenReturn(response);

        mockMvc.perform(put(BASE_URL)
                .principal(new UsernamePasswordAuthenticationToken(email, "password123"))
                .contentType(APPLICATION_JSON).content("""
                        {
                            "firstName": "Updated",
                            "lastName": "User",
                            "username": "updated.user",
                            "password": "password123",
                            "email": "employee.one@example.com",
                            "phone": "+251933333333"
                        }
                        """)).andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email));

        verify(userService).updateProfile(eq(email), any(ProfileUpdateDTO.class));
    }

    @Test
    void shouldAddProfilePicture() throws Exception {
        String email = "employee.one@example.com";
        String objectKey = "profiles/employee-one.jpg";
        UserResponseDTO response = sampleUser(UUID.randomUUID(), email);
        when(userService.addProfilePicture(email, objectKey)).thenReturn(response);

        mockMvc.perform(post(BASE_URL + "/profile-picture")
                .principal(new UsernamePasswordAuthenticationToken(email, "password123"))
                .contentType(APPLICATION_JSON)
                .content("{" + "\"objectKey\":\"" + objectKey + "\"}")).andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email));

        verify(userService).addProfilePicture(email, objectKey);
    }
}
