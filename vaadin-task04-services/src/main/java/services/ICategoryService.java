package services;

import entities.Category;
import org.springframework.stereotype.Service;

import java.util.List;


public interface ICategoryService extends IService<Category> {
    /**
     * save or update in DB category or return null if impossible
     *
     * @param category entity for save or update
     * @return saved or updated in DB category or
     *         return null if impossible
     */
    Category save(Category category);

    Category getCategoryByName(String categoryName);
    boolean existWithName(String categoryName);

    List<Category> getAllByFilter(String nameFilter);

}
