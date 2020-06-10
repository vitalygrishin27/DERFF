package app.services.impl;

import app.Models.DBLog;
import app.repository.DBLogRepository;
import app.services.DBLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DBLogServiceImpl implements DBLogService {
    @Autowired
    DBLogRepository repository;

    @Override
    public void save(DBLog dbLog) {
        repository.saveAndFlush(dbLog);
    }
}
