package services.impl;

import dao.ICategoryDao;
import dao.IHotelDao;
import entities.Category;
import entities.Hotel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import services.IHotelService;

import java.util.List;
import java.util.logging.Logger;

@Service
@Transactional(transactionManager = "txManager")
public class HotelService extends BaseService<Hotel> implements IHotelService {
    private static final Logger LOGGER = Logger.getLogger(HotelService.class.getName());

    private IHotelDao hotelDao;
    private ICategoryDao categoryDao;

    @Autowired
    public HotelService(IHotelDao hotelDao, ICategoryDao categoryDao) {
        super();
        this.hotelDao = hotelDao;
        this.categoryDao = categoryDao;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Hotel save(Hotel hotel) {
        // check not null hotel
        if (hotel == null) {
            // can't save such hotel in DB

            return null;
        }

        // check if Hotel.category is in DB
        Category inCategory = hotel.getCategory();
        Category categoryFromDB = inCategory != null
                ? categoryDao.get(inCategory.getId())   // persist category in Hibernate context !!!
                : null;
        if (inCategory == null || categoryFromDB == null) {
            // can't save or update hotel, category already deleted from DB
            return null;
        }

        // set hotel persisted category from DB         !!!
        hotel.setCategory(categoryFromDB);

        if (hotel.getId() == null) {
            // save in DB & persist in context new hotel

            return hotelDao.add(hotel);
        } else {
            // hotel exist in DB -> update entity

            return hotelDao.update(hotel);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Hotel bulkUpdate(Hotel hotel) {
        // check not null hotel
        if (hotel == null) {
            // can't save such hotel in DB

            return null;
        }

        // check if Hotel.category is in DB
        Category inCategory = hotel.getCategory();
        Category categoryFromDB = inCategory != null
                ? categoryDao.get(inCategory.getId())   // persist category in Hibernate context !!!
                : null;

        // set hotel persisted category from DB         !!!
        hotel.setCategory(categoryFromDB);

        if (hotel.getId() == null) {
            // hotel not exist in DB - can't update

            return null;
        } else {
            // hotel exist in DB -> update entity

            return hotelDao.update(hotel);
        }
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public List<Hotel> getAllByFilter(String nameFilter, String addressFilter) {
        return hotelDao.getAllByFilter(nameFilter, addressFilter);
    }
}
