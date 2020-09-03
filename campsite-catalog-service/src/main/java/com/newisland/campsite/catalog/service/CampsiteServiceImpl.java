package com.newisland.campsite.catalog.service;

import com.newisland.campsite.catalog.model.entity.Campsite;
import com.newisland.campsite.catalog.model.repository.CampsiteRepository;
import com.newisland.common.dto.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class CampsiteServiceImpl implements CampsiteService{

    @Autowired
    private CampsiteRepository campsiteRepository;

    @Override
    public Optional<Campsite> findById(UUID id) {
        return campsiteRepository.findById(id);
    }

    @Override
    @CachePut(cacheNames="campsitePage", key="#start-#pageSize")
    public PageResult<Campsite> findAllByPage(int start, int pageSize) {
        Page<Campsite> page = campsiteRepository.findAll(PageRequest.of(start,pageSize));
        return new PageResult<>(page.get().collect(Collectors.toList()), page.getTotalPages());
    }

    @Override
    @CacheEvict(cacheNames="campsitePage", allEntries=true)
    public Campsite save(Campsite campsite) {
        campsite.setCreatedOn(Instant.now());
        return campsiteRepository.save(campsite);
    }

    @Override
    public void delete(UUID id) {
        this.findById(id).ifPresent(campsiteRepository::delete);
    }
}
