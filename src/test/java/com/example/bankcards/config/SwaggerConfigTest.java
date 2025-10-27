package com.example.bankcards.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SwaggerConfigTest {

    @InjectMocks
    private SwaggerConfig swaggerConfig;

    @Test
    void customOpenAPI_ShouldReturnOpenAPI() {
        // When
        OpenAPI openAPI = swaggerConfig.customOpenAPI();

        // Then
        assertNotNull(openAPI);
        assertNotNull(openAPI.getInfo());
    }

    @Test
    void customOpenAPI_MultipleCalls_ShouldReturnNewInstance() {
        // When
        OpenAPI openAPI1 = swaggerConfig.customOpenAPI();
        OpenAPI openAPI2 = swaggerConfig.customOpenAPI();

        // Then
        assertNotSame(openAPI1, openAPI2);
        assertNotNull(openAPI1);
        assertNotNull(openAPI2);
    }

    @Test
    void customOpenAPI_ShouldHaveCorrectInfo() {
        // When
        OpenAPI openAPI = swaggerConfig.customOpenAPI();

        // Then
        assertNotNull(openAPI.getInfo());
        Info info = openAPI.getInfo();
        assertEquals("Bank Cards Management API", info.getTitle());
        assertEquals("1.0.0", info.getVersion());
        assertEquals("API для системы управления банковскими картами", info.getDescription());
    }

    @Test
    void customOpenAPI_ShouldHaveCorrectLicense() {
        // When
        OpenAPI openAPI = swaggerConfig.customOpenAPI();

        // Then
        assertNotNull(openAPI.getInfo());
        Info info = openAPI.getInfo();
        assertNotNull(info.getLicense());
        License license = info.getLicense();
        assertEquals("MIT License", license.getName());
        assertEquals("https://opensource.org/licenses/MIT", license.getUrl());
    }

    @Test
    void customOpenAPI_ShouldHaveSecurityRequirement() {
        // When
        OpenAPI openAPI = swaggerConfig.customOpenAPI();

        // Then
        assertNotNull(openAPI.getSecurity());
        assertFalse(openAPI.getSecurity().isEmpty());
        assertTrue(openAPI.getSecurity().stream()
                .anyMatch(securityRequirement -> 
                    securityRequirement.containsKey("Bearer Authentication")));
    }

    @Test
    void customOpenAPI_ShouldHaveSecurityScheme() {
        // When
        OpenAPI openAPI = swaggerConfig.customOpenAPI();

        // Then
        assertNotNull(openAPI.getComponents());
        assertNotNull(openAPI.getComponents().getSecuritySchemes());
        assertTrue(openAPI.getComponents().getSecuritySchemes().containsKey("Bearer Authentication"));
        
        SecurityScheme securityScheme = openAPI.getComponents().getSecuritySchemes().get("Bearer Authentication");
        assertNotNull(securityScheme);
        assertEquals(SecurityScheme.Type.HTTP, securityScheme.getType());
        assertEquals("bearer", securityScheme.getScheme());
        assertEquals("JWT", securityScheme.getBearerFormat());
        assertEquals("Введите JWT токен", securityScheme.getDescription());
    }

    @Test
    void customOpenAPI_ShouldHaveCorrectTitle() {
        // When
        OpenAPI openAPI = swaggerConfig.customOpenAPI();

        // Then
        assertNotNull(openAPI.getInfo());
        assertEquals("Bank Cards Management API", openAPI.getInfo().getTitle());
    }

    @Test
    void customOpenAPI_ShouldHaveCorrectVersion() {
        // When
        OpenAPI openAPI = swaggerConfig.customOpenAPI();

        // Then
        assertNotNull(openAPI.getInfo());
        assertEquals("1.0.0", openAPI.getInfo().getVersion());
    }

    @Test
    void customOpenAPI_ShouldHaveCorrectDescription() {
        // When
        OpenAPI openAPI = swaggerConfig.customOpenAPI();

        // Then
        assertNotNull(openAPI.getInfo());
        assertEquals("API для системы управления банковскими картами", openAPI.getInfo().getDescription());
    }

    @Test
    void customOpenAPI_ShouldHaveCorrectLicenseName() {
        // When
        OpenAPI openAPI = swaggerConfig.customOpenAPI();

        // Then
        assertNotNull(openAPI.getInfo());
        assertNotNull(openAPI.getInfo().getLicense());
        assertEquals("MIT License", openAPI.getInfo().getLicense().getName());
    }

    @Test
    void customOpenAPI_ShouldHaveCorrectLicenseUrl() {
        // When
        OpenAPI openAPI = swaggerConfig.customOpenAPI();

        // Then
        assertNotNull(openAPI.getInfo());
        assertNotNull(openAPI.getInfo().getLicense());
        assertEquals("https://opensource.org/licenses/MIT", openAPI.getInfo().getLicense().getUrl());
    }

    @Test
    void customOpenAPI_ShouldHaveComponents() {
        // When
        OpenAPI openAPI = swaggerConfig.customOpenAPI();

        // Then
        assertNotNull(openAPI.getComponents());
        assertNotNull(openAPI.getComponents().getSecuritySchemes());
    }

    @Test
    void customOpenAPI_ShouldHaveSecuritySchemes() {
        // When
        OpenAPI openAPI = swaggerConfig.customOpenAPI();

        // Then
        assertNotNull(openAPI.getComponents());
        assertNotNull(openAPI.getComponents().getSecuritySchemes());
        assertTrue(openAPI.getComponents().getSecuritySchemes().containsKey("Bearer Authentication"));
    }

    @Test
    void customOpenAPI_ShouldHaveSecurityRequirements() {
        // When
        OpenAPI openAPI = swaggerConfig.customOpenAPI();

        // Then
        assertNotNull(openAPI.getSecurity());
        assertFalse(openAPI.getSecurity().isEmpty());
    }

    @Test
    void customOpenAPI_ShouldHaveBearerAuthenticationSecurityRequirement() {
        // When
        OpenAPI openAPI = swaggerConfig.customOpenAPI();

        // Then
        assertNotNull(openAPI.getSecurity());
        assertTrue(openAPI.getSecurity().stream()
                .anyMatch(securityRequirement -> 
                    securityRequirement.containsKey("Bearer Authentication")));
    }

    @Test
    void customOpenAPI_ShouldHaveHttpSecurityScheme() {
        // When
        OpenAPI openAPI = swaggerConfig.customOpenAPI();

        // Then
        SecurityScheme securityScheme = openAPI.getComponents().getSecuritySchemes().get("Bearer Authentication");
        assertNotNull(securityScheme);
        assertEquals(SecurityScheme.Type.HTTP, securityScheme.getType());
    }

    @Test
    void customOpenAPI_ShouldHaveBearerSecurityScheme() {
        // When
        OpenAPI openAPI = swaggerConfig.customOpenAPI();

        // Then
        SecurityScheme securityScheme = openAPI.getComponents().getSecuritySchemes().get("Bearer Authentication");
        assertNotNull(securityScheme);
        assertEquals("bearer", securityScheme.getScheme());
    }

    @Test
    void customOpenAPI_ShouldHaveJwtBearerFormat() {
        // When
        OpenAPI openAPI = swaggerConfig.customOpenAPI();

        // Then
        SecurityScheme securityScheme = openAPI.getComponents().getSecuritySchemes().get("Bearer Authentication");
        assertNotNull(securityScheme);
        assertEquals("JWT", securityScheme.getBearerFormat());
    }

    @Test
    void customOpenAPI_ShouldHaveSecuritySchemeDescription() {
        // When
        OpenAPI openAPI = swaggerConfig.customOpenAPI();

        // Then
        SecurityScheme securityScheme = openAPI.getComponents().getSecuritySchemes().get("Bearer Authentication");
        assertNotNull(securityScheme);
        assertEquals("Введите JWT токен", securityScheme.getDescription());
    }
}
