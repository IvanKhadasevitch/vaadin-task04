package utils.entitydescription.impl;

import annotations.NoBulkUpdate;
import org.springframework.stereotype.Component;
import utils.entitydescription.vo.EntityFieldDescription;
import utils.entitydescription.IEntityUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Component                   // spring component - singleton by default
public class EntityUtil implements IEntityUtil {
    private volatile ConcurrentHashMap<Class<?>, Map<String,EntityFieldDescription>> entityDescriptionMap =
            new ConcurrentHashMap<>();

    public EntityUtil() { }

    @Override
    public <E> Map<String,EntityFieldDescription> getEntityDescription(Class<E> entityClass) {
        if (entityDescriptionMap.containsKey(entityClass)) {
            return entityDescriptionMap.get(entityClass);
        } else {
            return createEntityDescription(entityClass);
        }
    }

    private <E> Map<String,EntityFieldDescription> createEntityDescription(Class<E> entityClass) {
        Map<String,EntityFieldDescription> fieldDescriptionMap = new HashMap<>();

        Method[] methods = entityClass.getMethods();
        Field[] privateFields = entityClass.getDeclaredFields();
        for (Field field : privateFields) {
            // check suitable fields for safe description
            // annotation @NoBulkUpdate - forbid bulk updating for the field
            boolean fieldFilter = field.getAnnotation(NoBulkUpdate.class) == null;

            if (fieldFilter) {
                // create & determinate new field description
                String methodName = field.getName().length() == 1
                        ? field.getName().toUpperCase()
                        : field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);
                // save field description in Map
                fieldDescriptionMap.put(field.getName(),
                        new EntityFieldDescription(field.getName(), field.getType(),
                                this.getMethodByName(methods, "set" + methodName)));
            }
        }
        // save entity class description in Map
        entityDescriptionMap.put(entityClass, fieldDescriptionMap);

        return fieldDescriptionMap;
    }

    private Method getMethodByName(Method[] methods, String methodName) {
        for (Method method : methods) {
            if (methodName.equals(method.getName())) {
                return method;
            }
        }

        return null;
    }
}
