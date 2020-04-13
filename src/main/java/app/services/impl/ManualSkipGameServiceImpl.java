package app.services.impl;

import app.repository.ManualSkipGameRepository;
import app.services.ManualSkipGameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ManualSkipGameServiceImpl implements ManualSkipGameService {
    @Autowired
    ManualSkipGameRepository repository;
}
