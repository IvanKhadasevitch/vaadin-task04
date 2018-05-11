package dao.impl;

import dao.IDao;
import lombok.Getter;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Repository()
public class BaseDao<T> implements IDao<T> {
    private static final Logger LOGGER = Logger.getLogger(BaseDao.class.getName());

    @PersistenceContext
    @Getter
    private EntityManager em;

    Class<T> clazz;


    @Override
    public T add(T entity) {
        em.persist(entity);
        LOGGER.log(Level.INFO, "Save: " + entity);
        return entity;
    }

    @Override
    public T update(T entity) {
//        getEntityManager().flush();
        T result = null;
        em.merge(entity);
        em.flush();                         // ???????

        LOGGER.log(Level.INFO, "Update: " + entity);
        return entity;

    }

    @Override
    public T get(Serializable entityId) {
        // return null if entity not exist
        T entity = em.find(this.clazz, entityId);

        LOGGER.log(Level.INFO, String.format("Get entity [%s] with id [%s]. Entity: %s",
                this.clazz, entityId, entity));

        return entity;
    }

    @Override
    public void delete(Serializable entityId) {
        T findEntity = this.get(entityId);

        LOGGER.log(Level.INFO, "Delete: " + findEntity);
        em.remove(findEntity);
    }

    CriteriaQuery<T> getCriteriaQuery() {
        CriteriaBuilder cb = em.getCriteriaBuilder();

        CriteriaQuery<T> criteria = cb.createQuery(this.clazz);

        Root<T> root = criteria.from(this.clazz);
        criteria.select(root);

        return criteria;
    }

    T getSingleResultWhere(CriteriaQuery<T> criteria) {

        return em.createQuery(criteria).getSingleResult();
    }

    List<T> getListWhere(CriteriaQuery<T> criteria) {

        return em.createQuery(criteria).getResultList();
    }

    @Override
    public List<T> getAll() {
        List<T> resultList = em.createQuery(getCriteriaQuery()).getResultList();
        LOGGER.log(Level.INFO, "Get all entities: " + resultList);

        return resultList;
    }
}
