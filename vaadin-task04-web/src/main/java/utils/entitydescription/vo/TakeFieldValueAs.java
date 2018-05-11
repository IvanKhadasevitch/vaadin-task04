package utils.entitydescription.vo;


import com.vaadin.data.Binder;
import com.vaadin.data.HasValue;
import lombok.Getter;
import utils.entitydescription.vo.SingleValueBeen;

@Getter
public class TakeFieldValueAs {
    private Class<?> entityFieldClass;
    private Binder<?> binder;
    private Class<?> fieldValueClass;
    private HasValue<?> fieldValueTaker;

    public <V,T> TakeFieldValueAs(Class<T> entityFieldClass, Binder<SingleValueBeen<T>> binder,
                            Class<V> fieldValueClass, HasValue<V> fieldValueTaker) {
        this.entityFieldClass = entityFieldClass;
        this.binder = binder;
        this.fieldValueClass = fieldValueClass;
        this.fieldValueTaker = fieldValueTaker;
    }
}
