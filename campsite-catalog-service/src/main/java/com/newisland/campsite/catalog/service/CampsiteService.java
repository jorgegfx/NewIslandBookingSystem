package com.newisland.campsite.catalog.service;

import com.newisland.campsite.catalog.model.entity.Campsite;
import com.newisland.common.dto.PageResult;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CampsiteService {
    Optional<Campsite> findById(UUID id);
    PageResult<Campsite> findAll(int start, int pageSize);
    Campsite save(Campsite campsite);
    void delete(UUID id);
}
