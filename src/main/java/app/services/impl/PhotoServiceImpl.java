package app.services.impl;

import app.Models.Photo;
import app.repository.PhotoRepository;
import app.services.PhotoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PhotoServiceImpl implements PhotoService {
    @Autowired
    PhotoRepository repository;

    @Override
    public void save(Photo photo) {
        repository.save(photo);
    }

    @Override
    public Photo getPhotoById(long id) {
       return repository.findById(id).get();
    }
}
