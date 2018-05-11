package services.impl;

import dao.ICategoryDao;
import dao.IHotelDao;
import entities.Category;
import entities.Hotel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import services.ICategoryService;

import java.io.Serializable;
import java.util.List;
import java.util.logging.Logger;

@Service
@Transactional(transactionManager = "txManager")
public class CategoryService extends BaseService<Category> implements ICategoryService {
    private static final Logger LOGGER = Logger.getLogger(CategoryService.class.getName());

    private ICategoryDao categoryDao;
    private IHotelDao hotelDao;

    @Autowired
    public CategoryService(ICategoryDao categoryDao, IHotelDao hotelDao) {
        super();
        this.categoryDao = categoryDao;
        this.hotelDao = hotelDao;
    }

    @Override
    @Transactional(transactionManager = "txManager", propagation = Propagation.REQUIRED)
    public Category save(Category category) {
        if (category == null) {
            // such category can't save in DB
            return null;

        } else if (category.getId() == null) {
            // save in DB & persist in context new entity
            boolean isCategoryNameInDB = category.getName() != null &&
                    categoryDao.existWithName(category.getName());
            if (isCategoryNameInDB) {
                // such category name already in use - can't save in DB

                return null;
            } else {
                // save category in DB

                return categoryDao.add(category);
            }
        } else {
            // entity exist in DB -> update entity
            boolean isCategoryNameInDB = category.getName() != null &&
                    categoryDao.existWithName(category.getName());
            if (isCategoryNameInDB) {
                // such category name already in use - can't save in DB
                return null;
            } else {
                // update category in DB
                return categoryDao.update(category);
            }
        }
    }

    @Override
    @Transactional(transactionManager = "txManager", propagation = Propagation.REQUIRED)
    public void delete(Serializable categoryId) {
        List<Hotel> hotelList = hotelDao.getAllByCategory(categoryId);
        super.delete(categoryId);

        // update to null Category.id for all in hotelList
        hotelList.forEach(hotel -> {
            hotel.setCategory(null);
            hotelDao.update(hotel);
        } );
    }

    @Override
    public Category getCategoryByName(String categoryName) {
        return categoryDao.getCategoryByName(categoryName);
    }

    @Override
    public boolean existWithName(String categoryName) {

        return categoryDao.existWithName(categoryName);
    }

    @Override
    public List<Category> getAllByFilter(String nameFilter) {
        return categoryDao.getAllByFilter(nameFilter);
    }
}
