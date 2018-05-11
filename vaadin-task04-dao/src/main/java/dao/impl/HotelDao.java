package dao.impl;

import dao.IHotelDao;
import entities.Category;
import entities.Hotel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Repository
public class HotelDao extends BaseDao<Hotel> implements IHotelDao {
    private static final Logger LOGGER = Logger.getLogger(HotelDao.class.getName());

    @Autowired
    public HotelDao() {

        super();
        clazz = Hotel.class;
    }

    @Override
    public List<Hotel> getAllByFilter(String nameFilter, String addressFilter) {
        String inNameFilter = nameFilter == null ? "" : nameFilter;
        String inAddressFilter = addressFilter == null ? "" : addressFilter;

        if (inNameFilter.isEmpty() && inAddressFilter.isEmpty()) {
            return getAll();
        } else {
            CriteriaQuery<Hotel> criteria = getCriteriaQuery();
            CriteriaBuilder cb = getEm().getCriteriaBuilder();
            Root<Hotel> root = (Root<Hotel>) criteria.getRoots().toArray()[0];

            if (! inNameFilter.isEmpty() && ! inAddressFilter.isEmpty()) {
                // bough name & address filters
                Predicate predicateName = cb.like(root.get("name"), "%" + inNameFilter + "%");
                Predicate predicateAddress = cb.like(root.get("address"), "%" + inAddressFilter + "%");
                criteria.where(cb.and(predicateName, predicateAddress));
            } else if (! inNameFilter.isEmpty()) {
                // only nameFilter
                criteria.where(cb.like(root.get("name"), "%" + inNameFilter + "%"));
            } else {
                // only addressFilter
                criteria.where(cb.like(root.get("address"), "%" + inAddressFilter + "%"));
            }

            List<Hotel> resultList = getListWhere(criteria);
            LOGGER.log(Level.INFO, String.format("Get all Hotels where name contains [s] " +
                    "and address contains [s]: ", inNameFilter, inAddressFilter) + resultList);

            return resultList;
        }
    }

    @Override
    public List<Hotel> getAllByCategory(Serializable categoryId) {
        CriteriaQuery<Hotel> criteria = getCriteriaQuery();
        CriteriaBuilder cb = getEm().getCriteriaBuilder();
        Root<Hotel> root = (Root<Hotel>) criteria.getRoots().toArray()[0];

        criteria.where(cb.equal(root.get("category").get("id"), categoryId));

        List<Hotel> resultList = getListWhere(criteria);
        LOGGER.log(Level.INFO, "Get all Hotels where category id is: " + categoryId);

        return resultList;

    }
}
