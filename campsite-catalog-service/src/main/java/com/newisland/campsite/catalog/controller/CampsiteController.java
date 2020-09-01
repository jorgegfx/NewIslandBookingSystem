package com.newisland.campsite.catalog.controller;

import com.newisland.campsite.catalog.dto.CreateCampsiteRequest;
import com.newisland.campsite.catalog.model.entity.Campsite;
import com.newisland.campsite.catalog.service.CampsiteService;
import com.newisland.common.dto.PageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/campsite")
public class CampsiteController {
    @Autowired
    private CampsiteService campsiteService;

    @GetMapping("/{id}")
    public Mono<Campsite> getCampsiteById(@PathVariable String id) {
        return Mono.justOrEmpty(campsiteService.findById(UUID.fromString(id)));
    }

    @GetMapping("/all/{start}/{pageSize}")
    public Mono<PageResult> getAllCampsites(@PathVariable int start, @PathVariable int pageSize) {
        return Mono.just(campsiteService.findAll(start,pageSize));
    }

    @PostMapping
    @ResponseStatus(code = HttpStatus.CREATED)
    private Mono<Campsite> save(@RequestBody CreateCampsiteRequest createCampsiteRequest){
        try {
            return Mono.justOrEmpty(
                    campsiteService.save(createCampsiteRequest.toDomain()));
        }catch (Exception ex){
            String errorMessage = String.format("Error Creating campsite: %s ...",createCampsiteRequest);
            log.error(errorMessage,ex);
            return Mono.error(new IllegalStateException(errorMessage));
        }
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public Mono<Void> delete(@PathVariable String id){
        try{
            campsiteService.delete(UUID.fromString(id));
            return Mono.empty().then();
        }catch (Exception ex){
            String errorMessage = String.format("Error Deleting campsite: %s ...",id);
            log.error(errorMessage,ex);
            return Mono.error(new IllegalStateException(errorMessage));
        }
    }
}
