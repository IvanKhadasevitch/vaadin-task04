package dao;

import entities.Hotel;

import java.io.Serializable;
import java.util.List;

public interface IHotelDao extends IDao<Hotel> {
    List<Hotel> getAllByFilter(String nameFilter, String addressFilter);
    List<Hotel> getAllByCategory(Serializable categoryId);
}
