package dao.impl;

import dao.ICategoryDao;
import entities.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Repository
public class CategoryDao extends BaseDao<Category> implements ICategoryDao {
    private static final Logger LOGGER = Logger.getLogger(CategoryDao.class.getName());

    public CategoryDao() {

        super();
        clazz = Category.class;

        // delete after debug
        System.out.println("STOP -> CategoryDao constructor this: " + this);
    }

    @Override
    public Category getCategoryByName(String categoryName) {
        CriteriaQuery<Category> criteria = getCriteriaQuery();
        CriteriaBuilder cb = getEm().getCriteriaBuilder();
        Root<Category> root = (Root<Category>) criteria.getRoots().toArray()[0];

        criteria.where(cb.equal(root.get("name"), categoryName));

        Category category = null;

        try {
            category = getSingleResultWhere(criteria);
            LOGGER.log(Level.INFO, String.format("Category with name [%s] is got from DB %s", categoryName, category));
        } catch (NoResultException exception) {
            LOGGER.log(Level.WARNING, "There is no category in DB with name: " + categoryName);
        }

        return category;
    }

    @Override
    public boolean existWithName(String categoryName) {

        return getCategoryByName(categoryName) != null;
    }

    @Override
    public List<Category> getAllByFilter(String nameFilter) {
        if (nameFilter == null || nameFilter.isEmpty()) {
            return getAll();
        } else {
            CriteriaQuery<Category> criteria = getCriteriaQuery();
            CriteriaBuilder cb = getEm().getCriteriaBuilder();
            Root<Category> root = (Root<Category>) criteria.getRoots().toArray()[0];

            criteria.where(cb.like(root.get("name"), "%" + nameFilter + "%"));

            List<Category> resultList = getListWhere(criteria);
            LOGGER.log(Level.INFO, "Get all Categories where name contains [" + nameFilter + "]: " + resultList);

            return resultList;
        }


    }
}
