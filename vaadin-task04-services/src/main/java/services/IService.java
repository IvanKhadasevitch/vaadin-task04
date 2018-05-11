package services;

import java.io.Serializable;
import java.util.List;

public interface IService <T> {
//    T save(T entity);

//    T update(T entity);

    T get(Serializable id);

    void delete(Serializable id);

    List<T> getAll();
}
