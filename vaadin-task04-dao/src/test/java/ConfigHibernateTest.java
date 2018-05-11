import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.io.FileNotFoundException;
import java.sql.SQLException;

public class ConfigHibernateTest extends Assert {
    protected static EntityManagerFactory emf;
    protected static EntityManager em;

    @BeforeClass
    public static void init() throws FileNotFoundException, SQLException {
        emf = Persistence.createEntityManagerFactory("by.ivan.test");
        em = emf.createEntityManager();
    }

    @Test
    public void checkHibernateConfiguration(){
        System.out.println("Hibernate is configured");
    }

    @AfterClass
    public static void tearDown(){
        System.out.println("em.clear();");
        em.clear();
        System.out.println("em.close();");
        em.close();
        System.out.println("emf.close();");
        emf.close();
    }
}
