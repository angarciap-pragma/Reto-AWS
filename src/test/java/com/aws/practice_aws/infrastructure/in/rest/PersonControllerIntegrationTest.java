package com.aws.practice_aws.infrastructure.in.rest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.endsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.profiles.active=test")
@AutoConfigureMockMvc
class PersonControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldSaveAndFindPerson() throws Exception {
        String saveRequest = """
                {
                  "identificationNumber": "2001",
                  "name": "Andrea Garcia",
                  "email": "andrea@demo.com"
                }
                """;

        mockMvc.perform(post("/api/v1/personas/guardarpersona")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(saveRequest))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/v1/personas/consultarpersona/2001")))
                .andExpect(jsonPath("$.identificationNumber").value("2001"));

        mockMvc.perform(get("/api/v1/personas/consultarpersona/2001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Andrea Garcia"))
                .andExpect(jsonPath("$.email").value("andrea@demo.com"));
    }

    @Test
    void shouldReturnValidationError() throws Exception {
        String saveRequest = """
                {
                  "identificationNumber": "",
                  "name": "",
                  "email": "not-an-email"
                }
                """;

        mockMvc.perform(post("/api/v1/personas/guardarpersona")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(saveRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.details").isArray());
    }

    @Test
    void shouldReturnValidationErrorWhenIdentificationNumberIsTooLong() throws Exception {
        mockMvc.perform(get("/api/v1/personas/consultarpersona/1234567890123456789012345678901"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.details").isArray());
    }

    @Test
    void shouldReturnConflictWhenPersonAlreadyExists() throws Exception {
        String saveRequest = """
                {
                  "identificationNumber": "2002",
                  "name": "Andrea Garcia",
                  "email": "andrea@demo.com"
                }
                """;

        mockMvc.perform(post("/api/v1/personas/guardarpersona")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(saveRequest))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/personas/guardarpersona")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(saveRequest))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("RESOURCE_ALREADY_EXISTS"))
                .andExpect(jsonPath("$.message").value("La persona ya existe"));
    }

    @Test
    void shouldReturnNotFoundWhenPersonDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/v1/personas/consultarpersona/99999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("La persona no existe"));
    }

    @Test
    void shouldReturnRouteNotFoundWhenPathDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/v1/personas/no-existe"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ROUTE_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("La ruta solicitada no existe"))
                .andExpect(jsonPath("$.details[0]").value("No existe un endpoint para la ruta solicitada"));
    }

    @Test
    void shouldReturnMethodNotAllowedWhenHttpMethodIsInvalid() throws Exception {
        mockMvc.perform(put("/api/v1/personas/guardarpersona")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "identificationNumber": "3001",
                                  "name": "Andrea Garcia",
                                  "email": "andrea@demo.com"
                                }
                                """))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.code").value("METHOD_NOT_ALLOWED"))
                .andExpect(jsonPath("$.message").value("El metodo HTTP no esta permitido para esta ruta"))
                .andExpect(jsonPath("$.details[0]").exists());
    }

    @Test
    void shouldReturnUnsupportedMediaTypeWhenContentTypeIsInvalid() throws Exception {
        mockMvc.perform(post("/api/v1/personas/guardarpersona")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("contenido-invalido"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.code").value("UNSUPPORTED_MEDIA_TYPE"))
                .andExpect(jsonPath("$.message").value("El tipo de contenido enviado no es soportado"))
                .andExpect(jsonPath("$.details[0]").exists());
    }

    @Test
    void shouldReturnNotAcceptableWhenAcceptHeaderIsInvalid() throws Exception {
        mockMvc.perform(get("/api/v1/personas/consultarpersona/2001")
                        .accept(MediaType.APPLICATION_XML))
                .andExpect(status().isNotAcceptable())
                .andExpect(header().string("Accept", "[application/json]"));
    }
}
