package utils.entitydescription.impl;

import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import org.springframework.stereotype.Component;
import utils.entitydescription.IVaadinComponentUtil;

@Component                   // spring component - singleton by default
public class VaadinComponentUtil implements IVaadinComponentUtil {

    public VaadinComponentUtil() { }

    @Override
    public TextField getStandardTextField(String caption, String description, Boolean required) {
        TextField textField = new TextField();
        setCaptionAndDescription(caption, description, required, textField);
        textField.addStyleName(ValoTheme.TEXTFIELD_SMALL);


        return textField;
    }

    @Override
    public DateField getStandardDateField(String caption, String description, Boolean required) {
        DateField dateField = new DateField();
        setCaptionAndDescription(caption, description, required, dateField);
        dateField.addStyleName(ValoTheme.DATEFIELD_SMALL);

        return dateField;
    }

    @Override
    public TextArea getStandardTextArea(String caption, String description, Boolean required) {
        TextArea textArea = new TextArea();
        setCaptionAndDescription(caption, description, required, textArea);
        textArea.addStyleName(ValoTheme.TEXTAREA_SMALL);

        return textArea;
    }

    @Override
    public Button getStandardPrimaryButton(String caption, String description) {
        Button button = new Button();
        setCaptionAndDescription(caption, description, null, button);
        button.setStyleName(ValoTheme.BUTTON_SMALL);

        return button;
    }

    @Override
    public Button getStandardFriendlyButton(String caption, String description) {
        Button button = getStandardPrimaryButton(caption, description);
        button.addStyleName(ValoTheme.BUTTON_FRIENDLY);

        return button;
    }

    @Override
    public Button getStandardDangerButton(String caption, String description) {
        Button button = getStandardPrimaryButton(caption, description);
        button.addStyleName(ValoTheme.BUTTON_DANGER);

        return button;
    }

    private void setCaptionAndDescription(String caption, String description, Boolean required,
                                          AbstractComponent component) {
        if (caption != null) {
            component.setCaption(caption);
        }
        if (description != null) {
            component.setDescription(description);
        }
        if (required != null) {
            // Required field = true
            ((AbstractField) component).setRequiredIndicatorVisible(required);
        }
    }
}
