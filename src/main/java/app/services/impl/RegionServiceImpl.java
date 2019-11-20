package app.services.impl;

import app.Models.Region;
import app.repository.RegionRepository;
import app.services.RegionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RegionServiceImpl implements RegionService {
    @Autowired
    RegionRepository repository;

    @Override
    public void save(Region region) {
        repository.saveAndFlush(region);
    }

    @Override
    public Region findRegionByName(String name) {
        return repository.findRegionByName(name);
    }

    @Override
    public Region findRegionById(Long id) {
        return repository.getOne(id);
    }

    @Override
    public List<Region> findAllRegions() {
        return  repository.findAll();
    }

    @Override
    public void deleteById(long id) {
        repository.deleteById(id);
    }
}
