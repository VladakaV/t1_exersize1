package ru.t1.java.demo.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.t1.java.demo.model.DataSourceErrorLog;
import ru.t1.java.demo.repository.DataSourceErrorLogRepository;
import ru.t1.java.demo.service.DataSourceErrorLogService;

import java.util.List;

@Service
public class DataSourceErrorLogImpl implements DataSourceErrorLogService {

    @Autowired
    private DataSourceErrorLogRepository repository;

    public void saveLog(DataSourceErrorLog log) {
        repository.save(log);
    }

    public List<DataSourceErrorLog> findAll() {
        return repository.findAll();
    }
}
