package dao;

import java.io.Serializable;
import java.util.List;

public interface IDao<T> {
    T add(T t);

    T update(T t);

    T get(Serializable entityId);

    void delete(Serializable entityId);

    List<T> getAll();
}
