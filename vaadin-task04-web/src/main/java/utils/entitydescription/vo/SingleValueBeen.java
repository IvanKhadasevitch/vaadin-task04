package utils.entitydescription.vo;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter @Setter
public class SingleValueBeen<T> implements Serializable {
    private T value;

    public SingleValueBeen(T value) {
        this.value = value;
    }
}
