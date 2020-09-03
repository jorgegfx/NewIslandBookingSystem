package com.newisland.campsite.catalog.dto;

import com.newisland.campsite.catalog.model.entity.Campsite;
import lombok.Data;

@Data
public class CreateCampsiteRequest {
    private String name;
    private Double longitude;
    private Double latitude;

    public Campsite toDomain(){
        return Campsite.builder().
                name(name).
                latitude(latitude).
                longitude(longitude).build();
    }
}
