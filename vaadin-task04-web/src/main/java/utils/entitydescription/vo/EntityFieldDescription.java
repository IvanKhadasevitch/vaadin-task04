package utils.entitydescription.vo;

import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Method;

public class EntityFieldDescription<T> {
    public static final String SELECT_FIELD_PLACEHOLDER = "Please select field";
    public static final String VALUE_FIELD_PLACEHOLDER = "Enter field value";

    @Getter
    private Method methodSet;
    @Getter
    private Class<T> fieldClass;
    @Getter @Setter
    private String fieldCaption;

    public EntityFieldDescription(String fieldCaption, Class<T> fieldClass, Method methodSet) {
        this.fieldClass = fieldClass;
        this.fieldCaption = fieldCaption == null ? "" : fieldCaption;
        this.fieldCaption = this.fieldCaption.length() == 1
                ? this.fieldCaption.toUpperCase()
                : this.fieldCaption.substring(0, 1).toUpperCase() + this.fieldCaption.substring(1);
        this.methodSet = methodSet;
    }

}
