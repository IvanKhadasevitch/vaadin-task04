package services.impl;

import dao.IDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import services.IService;

import java.io.Serializable;
import java.util.List;
import java.util.logging.Logger;

@Service
@Transactional(transactionManager = "txManager")
public class BaseService<T> implements IService<T> {
    private static final Logger LOGGER = Logger.getLogger(BaseService.class.getName());

    @Autowired
    private IDao<T> baseDao;

    public BaseService() {
    }

    @Override
    public T get(Serializable id) {
        return baseDao.get(id);
    }

    @Override
    public void delete(Serializable id) {
        baseDao.delete(id);
    }

    @Override
    public List<T> getAll() {
        return baseDao.getAll();
    }
}
