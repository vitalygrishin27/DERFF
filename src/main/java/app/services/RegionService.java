package app.services;


import app.Models.Region;

import java.util.List;

public interface RegionService {
    void save(Region region);

    Region findRegionByName(String name);

    Region findRegionById(Long id);

    List<Region> findAllRegions();

    void deleteById(long id);
}
