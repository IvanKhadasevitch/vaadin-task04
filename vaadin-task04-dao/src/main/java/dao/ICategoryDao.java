package dao;

import entities.Category;

import java.util.List;

public interface ICategoryDao extends IDao<Category> {
    Category getCategoryByName(String categoryName);
    boolean existWithName(String categoryName);

    List<Category> getAllByFilter(String nameFilter);
}
