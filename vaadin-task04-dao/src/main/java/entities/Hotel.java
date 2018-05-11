package entities;

import annotations.NoBulkUpdate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.OptimisticLockType;
import org.hibernate.annotations.OptimisticLocking;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;

import static javax.persistence.AccessType.PROPERTY;

@Entity
@Table(name = "HOTEL")
@OptimisticLocking(type = OptimisticLockType.VERSION)
@DynamicUpdate              // will updated only changed fields of entity

@Setter @Getter
@NoArgsConstructor
public class Hotel implements Serializable, Cloneable {
    @NoBulkUpdate
    public static final Integer MAX_RATING = 5;
    @NoBulkUpdate
    public static final Integer MIN_RATING = 0;

    @Id
    //for auto_increment id - use IDENTITY
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false)
    @Access(PROPERTY)
    @NoBulkUpdate
    // ID bigint(20) PRIMARY KEY NOT NULL AUTO_INCREMENT,
    private Long id;

    // 	NAME	varchar(255)	NOT	NULL
    @Column(name = "NAME", nullable = false, length = 255)
    @Access(PROPERTY)
    private String name = "";

    // 	ADDRESS	varchar(255)	NOT	NULL
    @Column(name = "ADDRESS", nullable = false, length = 255)
    @Access(PROPERTY)
    private String address = "";

    // 	RATING	int(11)	NOT	NULL
    @Column(name = "RATING", nullable = false)
    @Access(PROPERTY)
    private Integer rating = 0;

    // 	OPERATES_FROM	bigint(20)	NOT	NULL
    @Column(name = "OPERATES_FROM", nullable = false)
    @Access(PROPERTY)
    private Long operatesFrom = LocalDate.now().minusDays(1).toEpochDay();

    // 	CATEGORY_ID	bigint(20)	YES	NULL
    @ManyToOne
//    @Column(name = "CATEGORY_ID")
    @Access(PROPERTY)
    private Category category = new Category(null);

    // 	URL	varchar(255)	NOT	NULL
    @Column(name = "URL", nullable = false, length = 255)
    @Access(PROPERTY)
    private String url = "";

    // 	DESCRIPTION	varchar(255) YES NULL
    @Column(name = "DESCRIPTION", nullable = true, length = 255)
    @Access(PROPERTY)
    private String description = "";

    // 	OPTLOCK	bigint(20)	YES NULL
    @Version
    @Column(name = "OPTLOCK")
    @Access(PROPERTY)
    @NoBulkUpdate
    private Long optLock;

    public Hotel(String name, String address, Integer rating, Long operatesFrom, String url) {
        this.name = name == null ? "" : name;
        this.address = address == null ? "" : address;
        this.rating = rating == null ? 0 : rating;
        this.operatesFrom = operatesFrom == null ? LocalDate.now().toEpochDay() : operatesFrom;
        this.url = url == null ? "" : url;
    }

    @Override
    public String toString() {
        return "Hotel{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", rating=" + rating +
                ", operatesFrom=" + operatesFrom +
                ", category=" + category +
                ", url='" + url + '\'' +
                '}';
    }
}
