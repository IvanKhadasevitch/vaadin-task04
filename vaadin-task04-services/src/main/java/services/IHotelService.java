package services;

import entities.Hotel;

import java.util.List;

public interface IHotelService extends IService<Hotel> {
    /**
     * save or update in DB hotel or return null if impossible
     *
     * @param hotel entity for save or update
     * @return saved or updated in DB hotel or
     *         return null if impossible
     */
    Hotel save(Hotel hotel);

    /**
     * update in Db hotel when use bulk update for the same field of selected hotels.
     * Does not check for changes in other fields
     * @param hotel entity for update
     * @return updated entity or null if updating impossible
     */
    Hotel bulkUpdate(Hotel hotel);

    List<Hotel> getAllByFilter(String nameFilter, String addressFilter);
}
