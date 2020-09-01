package com.newisland.campsite.catalog.model.repository;

import com.newisland.campsite.catalog.model.entity.Campsite;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.UUID;

public interface CampsiteRepository extends PagingAndSortingRepository<Campsite, UUID> {
}
