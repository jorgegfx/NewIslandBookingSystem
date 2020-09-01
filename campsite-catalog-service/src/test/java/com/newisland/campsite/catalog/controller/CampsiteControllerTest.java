package com.newisland.campsite.catalog.controller;

import com.newisland.campsite.catalog.TestApp;
import com.newisland.campsite.catalog.model.entity.Campsite;
import com.newisland.campsite.catalog.service.CampsiteService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@WebFluxTest(CampsiteController.class)
@ContextConfiguration(classes= TestApp.class)
class CampsiteControllerTest {
    @Autowired
    WebTestClient webTestClient;

    @MockBean
    private CampsiteService campsiteService;


    @Test
    public void testFindUserById(){
        UUID id = UUID.randomUUID();
        Campsite campsite = Campsite.builder().
                name("Test").longitude(1D).latitude(1D).build();
        when(campsiteService.findById(id)).thenReturn(Optional.of(campsite));
        webTestClient.get()
                .uri("/campsite/"+id)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Campsite.class)
                .value(resUser -> resUser.getName(), equalTo("Test"));
    }
}