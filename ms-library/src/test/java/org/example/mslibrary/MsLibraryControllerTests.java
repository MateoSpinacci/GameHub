package org.example.mslibrary;

import org.example.mslibrary.config.SecurityConfig;
import org.example.mslibrary.controller.LibraryController;
import org.example.mslibrary.service.LibraryService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@WebMvcTest(LibraryController.class)
@Import(SecurityConfig.class)
public class MsLibraryControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LibraryService service;

    @Test
    void shouldRejectUnauthenticatedRequest() throws Exception {
        mockMvc.perform(get("/api/library"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(service);
    }

    @Test
    void shouldAllowAuthenticatedUserToGetLibrary() throws Exception {
        when(service.getAllGames(any(Jwt.class)))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/library")
                .with(jwt()
                .authorities(new SimpleGrantedAuthority("ROLE_USER")))
        ).andExpect(status().isOk());

        verify(service).getAllGames(any(Jwt.class));
    }

    @Test
    void shouldRejectDeleteForUserRole() throws Exception {
        mockMvc.perform(delete("/api/library/1")
                .with(jwt()
                .authorities(new SimpleGrantedAuthority("ROLE_USER")))
        ).andExpect(status().isForbidden());

        verifyNoInteractions(service);
    }

    @Test
    void shouldAllowDeleteForAdminRole() throws Exception {
        mockMvc.perform(delete("/api/library/1")
                .with(jwt()
                        .jwt(token -> token.subject("usuario-123"))
                        .authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
        ).andExpect(status().isNoContent());

        ArgumentCaptor<Jwt> jwtCaptor = ArgumentCaptor.forClass(Jwt.class);

        verify(service).deleteGame(eq(1L), jwtCaptor.capture());
        assertEquals("usuario-123", jwtCaptor.getValue().getSubject());
    }
}
