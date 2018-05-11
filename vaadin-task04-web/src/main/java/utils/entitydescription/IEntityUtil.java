package utils.entitydescription;

import utils.entitydescription.vo.EntityFieldDescription;

import java.util.Map;

public interface IEntityUtil {
    /**
     * prepare Description of fields for entity with entityClass
     *
     * @param entityClass
     * @param <T> entity type
     * @return retern Map<K,V> vere K - entityClass name; V - EntityFieldDescription
     */
    <T> Map<String,EntityFieldDescription> getEntityDescription(Class<T> entityClass);
}
