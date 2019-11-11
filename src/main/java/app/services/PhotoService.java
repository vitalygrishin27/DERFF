package app.services;

import app.Models.Photo;

public interface PhotoService {
    void save(Photo photo);

    Photo getPhotoById(long id);
}
