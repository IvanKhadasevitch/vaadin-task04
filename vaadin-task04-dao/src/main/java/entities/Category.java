package entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.OptimisticLockType;
import org.hibernate.annotations.OptimisticLocking;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static javax.persistence.AccessType.PROPERTY;

@Entity
//@Table (name = "Persons", schema="test")
@Table(name = "CATEGORY")
@OptimisticLocking(type = OptimisticLockType.VERSION)
@DynamicUpdate              // will updated only changed fields of entity

@Setter @Getter
@NoArgsConstructor
public class Category implements Serializable, Cloneable {

    public final static String NULL_CATEGORY_REPRESENTATION = "No category";
    @Id
    //for auto_increment id - use IDENTITY
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false)
    @Access(PROPERTY)
    // ID bigint(20) PRIMARY KEY NOT NULL AUTO_INCREMENT,
    private Long id;

    // 	NAME	varchar(255)	NOT	NULL UNI
    @Column(name = "NAME", unique = true, nullable = false, length = 255)
    @Access(PROPERTY)
    private String name = "";

    // 	OPTLOCK	bigint(20)	YES NULL
    @Version
    @Column(name = "OPTLOCK")
    @Access(PROPERTY)
    private Long optLock;

    //don't save in DB as field of Table CATEGORY
    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH},
            fetch = FetchType.LAZY,
            orphanRemoval = false,
            mappedBy = "category")
    @BatchSize(size = 12)     //specify the size for batch loading the entries of a lazy collection.
    @Access(PROPERTY)
    private List<Hotel> hotelList = new ArrayList<>();

    public Category(String name) {
        this.name = name == null
                ? ""
                : name;
    }

    public void addHotel(Hotel hotel) {
        hotelList.add( hotel );
        hotel.setCategory( this );
    }

    public void removeHotel(Hotel hotel) {
        hotelList.remove( hotel );
        hotel.setCategory( null );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Category)) return false;

        Category category = (Category) o;

        if (!getId().equals(category.getId())) return false;
        return getName().equals(category.getName());
    }

    @Override
    public int hashCode() {
        int result = getId().hashCode();
        result = 31 * result + getName().hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Category{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
