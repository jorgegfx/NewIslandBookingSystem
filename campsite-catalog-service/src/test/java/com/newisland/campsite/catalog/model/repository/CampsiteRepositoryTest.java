package com.newisland.campsite.catalog.model.repository;

import com.newisland.campsite.catalog.TestApp;
import com.newisland.campsite.catalog.model.entity.Campsite;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.transaction.Transactional;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@Transactional
@SpringBootTest(classes = TestApp.class)
class CampsiteRepositoryTest {
    @Autowired
    private CampsiteRepository campsiteRepository;

    @Test
    public void findSavedUserById() {
        Campsite campsite = Campsite.builder().
                name("Test").
                longitude(1D).
                latitude(1D).
                createdOn(Instant.now()).build();
        Campsite expectedCampsite = campsiteRepository.save(campsite);
        assertThat(campsiteRepository.findById(campsite.getId())).hasValue(expectedCampsite);
    }
}