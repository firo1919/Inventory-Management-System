package com.firomsa.inventory.v1.controller.unitTest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.firomsa.inventory.v1.controller.UploadController;
import com.firomsa.inventory.v1.dto.UploadRequestDTO;
import com.firomsa.inventory.v1.dto.UploadResponseDTO;
import com.firomsa.inventory.v1.service.StorageService;

@WebMvcTest(UploadController.class)
@AutoConfigureMockMvc(addFilters = false)
public class UploadControllerUnitTest {

    @MockitoBean
    private StorageService storageService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldCreateUploadPresignTicket() throws Exception {
        UploadResponseDTO response =
                new UploadResponseDTO("12345_file.jpg", "https://signed.example.com/upload", "10");
        when(storageService.createUploadPresignTicket(any(UploadRequestDTO.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/uploads/presign").contentType(APPLICATION_JSON).content("""
                {
                    "filename": "file.jpg",
                    "contentType": "image/jpeg"
                }
                """)).andExpect(status().isOk())
                .andExpect(jsonPath("$.objectKey").value("12345_file.jpg"))
                .andExpect(jsonPath("$.uploadUrl").value("https://signed.example.com/upload"))
                .andExpect(jsonPath("$.expiresIn").value("10"));

        verify(storageService).createUploadPresignTicket(any(UploadRequestDTO.class));
    }
}
