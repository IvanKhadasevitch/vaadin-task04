package views.hotelveiw;

import com.vaadin.data.Binder;
import com.vaadin.data.HasValue;
import com.vaadin.data.converter.StringToIntegerConverter;
import com.vaadin.data.validator.DateRangeValidator;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import entities.Category;
import entities.Hotel;
import lombok.Getter;
import services.ICategoryService;
import services.IHotelService;
import ui.customcompanents.TopCenterComposite;
import utils.GetBeenFromSpringContext;
import utils.entitydescription.IVaadinComponentUtil;
import utils.entitydescription.vo.EntityFieldDescription;
import utils.entitydescription.IEntityUtil;
import utils.entitydescription.vo.SingleValueBeen;
import utils.entitydescription.vo.TakeFieldValueAs;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import static utils.entitydescription.vo.EntityFieldDescription.SELECT_FIELD_PLACEHOLDER;
import static utils.entitydescription.vo.EntityFieldDescription.VALUE_FIELD_PLACEHOLDER;

public class HotelBulkUpdate {
    private static final Logger LOGGER = Logger.getLogger(HotelBulkUpdate.class.getName());

    private HotelView hotelView;

    private IEntityUtil entityUtil;
    private IVaadinComponentUtil vaadinComponentUtil;
    private IHotelService hotelService;
    private ICategoryService categoryService;

    private HashMap<String, TakeFieldValueAs> hotelFieldsBindingRules = new HashMap<>();

    private final ComboBox<Map.Entry<String, EntityFieldDescription>> entityFieldsComboBox = new ComboBox<>();

    private HasValue<?> fieldValue = new TextField();              

    private Button updateFieldBtn;
    private Button cancelBtn;

    private VerticalLayout popupContent;
    private Component buttons;
    @Getter
    private PopupView popup;

    public HotelBulkUpdate(HotelView hotelView) {
        this.hotelView = hotelView;

        // get beans: entityUtil, vaadinComponentUtil, hotelService, categoryService
        // from Application context
        this.entityUtil = GetBeenFromSpringContext.getBeen(IEntityUtil.class);
        this.vaadinComponentUtil = GetBeenFromSpringContext.getBeen(IVaadinComponentUtil.class);
        this.hotelService = GetBeenFromSpringContext.getBeen(IHotelService.class);
        this.categoryService = GetBeenFromSpringContext.getBeen(ICategoryService.class);

        configureEntityDescription();
        configureComponents();
        buildLayout();
    }

    private void configureComponents() {
        // set entity field
        entityFieldsComboBox.addStyleName(ValoTheme.COMBOBOX_SMALL);
        entityFieldsComboBox.setEmptySelectionAllowed(false);
        entityFieldsComboBox.setPlaceholder(SELECT_FIELD_PLACEHOLDER);

        Map<String, EntityFieldDescription> hotelFields = entityUtil.getEntityDescription(Hotel.class);
        Set<Map.Entry<String, EntityFieldDescription>> hotelFieldsSet = hotelFields.entrySet();
        entityFieldsComboBox.setItems(hotelFieldsSet);
        entityFieldsComboBox.setItemCaptionGenerator(mapEntry -> mapEntry.getValue().getFieldCaption());
        entityFieldsComboBox.addValueChangeListener(event -> {
            popupContent.removeComponent((Component)fieldValue);
            popupContent.removeComponent(buttons);

            // determine for value field capturing AbstractField or SingleSelect
            String entityFieldName = event.getValue().getKey();
            fieldValue = hotelFieldsBindingRules.get(entityFieldName).getFieldValueTaker();

            // delete after debug
            System.out.println();
            System.out.println("*************************************");
            System.out.println("was click valueChangeListener, fieldValue: " + fieldValue);

            popupContent.addComponent((Component)fieldValue);
            popupContent.addComponent(buttons);
            popupContent.setComponentAlignment(buttons, Alignment.TOP_CENTER);
        });

        // set entity field value - for default picture
        ((TextField) fieldValue).addStyleName(ValoTheme.TEXTFIELD_SMALL);
        ((TextField) fieldValue).setPlaceholder(VALUE_FIELD_PLACEHOLDER);

        // update button
        String caption = "Update";
        String description = "Save changed value in data base";
        updateFieldBtn = vaadinComponentUtil.getStandardFriendlyButton(caption, description);
        updateFieldBtn.addClickListener(click -> {
            Map.Entry<String, EntityFieldDescription> fieldDescriptionEntry = entityFieldsComboBox.getValue();
            Set<Hotel> selectedHotels = hotelView.getHotelGrid().getSelectedItems();

            // delete after debug
            System.out.println();
            System.out.println("-------- ClickListener event. Start Updating hotels: ------------");
            selectedHotels.forEach(System.out::println);

            // validate field value
            Binder binder = hotelFieldsBindingRules.get(fieldDescriptionEntry.getKey())
                                                   .getBinder();
            boolean isErrors = binder.validate()
                                     .hasErrors();
            if (isErrors) {
                // is validation errors - nothing to do !!!
                Notification.show("Invalid field value", Notification.Type.WARNING_MESSAGE);

                return;
            }

            // try updating valid field value!!!
            int count = this.bulkUpdate(selectedHotels, fieldDescriptionEntry);

            // take fresh hotels from DB
            this.hotelView.updateHotelList();
            // take fresh categories from DB                !!!!!!
            this.updateCategoryNativeSelectItems();

            if (count == 0) {
                // no any updated
                Notification.show("Everything remained unchanged. Close form and try again. Maybe someone has already changed the selected hotels",
                        Notification.Type.ERROR_MESSAGE);
            } else {
                // somethings updated
                this.popup.setPopupVisible(false);

                if (count == selectedHotels.size()) {
                    //all were updated
                    Notification.show(String.format("All hotels updated: [%d] of [%d]",
                            count, selectedHotels.size()), Notification.Type.WARNING_MESSAGE);
                } else {
                    // not all were updated
                    Notification.show(String.format("Were updated [%d] of [%d] hotels. Maybe someone has already changed the selected hotels",
                            count, selectedHotels.size()), Notification.Type.ERROR_MESSAGE);
                }
            }

        });

        // button "Cancel"
        caption = "Cancel";
        description = "Close form without saving changes in data base";
        cancelBtn = vaadinComponentUtil.getStandardPrimaryButton(caption, description);
        cancelBtn.addClickListener(click -> {
            this.popup.setPopupVisible(false);
            // take fresh categories from DB
            this.updateCategoryNativeSelectItems();
        });

    }

    private void buildLayout() {
        // Content for the PopupView
        popupContent = new VerticalLayout();
        popupContent.setSpacing(true);
        popupContent.setMargin(true);
        popupContent.setSizeFull();         // set 100% x 100%

        Label label = new Label("Bulk update");
        label.addStyleName(ValoTheme.LABEL_NO_MARGIN);
        label.addStyleName(ValoTheme.LABEL_COLORED);
        popupContent.addComponent(label);
        popupContent.addComponent(entityFieldsComboBox);

        // field value catch
        popupContent.addComponent((Component) fieldValue);

        // buttons
        Component[] buttonComponents = {updateFieldBtn, cancelBtn};
        buttons = new TopCenterComposite(buttonComponents);
        popupContent.addComponent(buttons);
        popupContent.setComponentAlignment(buttons, Alignment.TOP_CENTER);

        // The popup component itself
        popup = new PopupView(null, popupContent);  // small=null -> invisible
        popup.setHideOnMouseOut(false);

    }

    // base functionality !!!
    private int bulkUpdate(Set<Hotel> selectedHotels,
                           Map.Entry<String, EntityFieldDescription> fieldDescriptionEntry) {
        //delete after debug
        System.out.println("start -> HotelBulkUpdate.bulkUpdate");

        String fieldName = fieldDescriptionEntry.getKey();
        EntityFieldDescription entityFieldDescription = fieldDescriptionEntry.getValue();

        //delete after debug
        System.out.println("fieldName: " + fieldName);

        // validate field value
        Binder binder = hotelFieldsBindingRules.get(fieldDescriptionEntry.getKey())
                                               .getBinder();

        // delete after debug
        System.out.println("binder: " + binder);

        boolean isErrors = binder.validate()
                                    .hasErrors();           //  !!!!!------!!!!!-----!!!!!
        int count = 0;
        if ( ! isErrors) {
            // validation passed - can update
            // take validated data field from binder to SingleValueBeen

            SingleValueBeen singleValueBeen = new SingleValueBeen(null);
            binder.writeBeanIfValid(singleValueBeen);

            //delete after debug
            System.out.println("singleValueBeen: " + singleValueBeen);

            Method setMethod = entityFieldDescription.getMethodSet();
            boolean isUpdated = false;
            for (Hotel hotel : selectedHotels) {
                try {
                    setMethod.invoke(hotel,
                            entityFieldDescription.getFieldClass().cast(singleValueBeen.getValue()));

                    // delete after debug
                    System.out.println("hotel after invoke: " + hotel);

                    isUpdated = hotelService.bulkUpdate(hotel) != null;

                    // delete after debug
                    System.out.println("isUpdated: " + isUpdated);

                    if (isUpdated) {
                        count++;
                    }
                } catch (Exception exp) {
                    LOGGER.log(Level.WARNING, "Can't update hotel: " + hotel, exp);
                }
            }

        }
        //delete after debug
        System.out.println("STOP -> HotelBulkUpdate.bulkUpdate");

        return count;
    }

    private void configureEntityDescription() {
        // configure Entity Hotel
        // configure "name" field
        String description = "Name of the hotel";           // Required field
        TextField nameTextField = vaadinComponentUtil.getStandardTextField(null,
                description, true);

        Binder<SingleValueBeen<String>> binderName = new Binder<>();
        binderName.forField(nameTextField)
                      .asRequired("Every hotel must have name")
                      .bind(SingleValueBeen::getValue, SingleValueBeen::setValue);

        TakeFieldValueAs takeFieldValueAs = new TakeFieldValueAs(String.class, binderName
                , String.class, nameTextField);
        // save "name" configuration
        hotelFieldsBindingRules.put("name", takeFieldValueAs);

        // configure "address" field
        description = "Address of the hotel";           // Required field
        TextField addressTextField = vaadinComponentUtil.getStandardTextField(null,
                description, true);

        Binder<SingleValueBeen<String>> binderAddress = new Binder<>();
        binderAddress.forField(addressTextField)
                  .asRequired("Every hotel must have address")
                  .bind(SingleValueBeen::getValue, SingleValueBeen::setValue);

        takeFieldValueAs = new TakeFieldValueAs(String.class, binderAddress,
                String.class, addressTextField);
        // save "address" configuration
        hotelFieldsBindingRules.put("address", takeFieldValueAs);

        // configure "rating" field
        description = "Rating of the hotel must be in [0;5]";           // Required field
        TextField ratingTextField = vaadinComponentUtil.getStandardTextField(null,
                description, true);

        Binder<SingleValueBeen<Integer>> binderRating = new Binder<>();
        binderRating.forField(ratingTextField)
                .asRequired("Every hotel must have a rating")
                .withConverter(new StringToIntegerConverter("Enter an integer, please"))
                .withValidator(rating -> rating >= 0 && rating <= 5,
                        "Rating must be in [0;5]")
                .bind(SingleValueBeen::getValue, SingleValueBeen::setValue);

        takeFieldValueAs = new TakeFieldValueAs(Integer.class, binderRating,
                String.class, ratingTextField);
        // save "rating" configuration
        hotelFieldsBindingRules.put("rating", takeFieldValueAs);

        // configure "operatesFrom" field
        description = "Opening day of the hotel";           // Required field
        DateField operatesFromDateField = vaadinComponentUtil.getStandardDateField(null,
                description, true);
        // set default value as yesterday
        operatesFromDateField.setValue(LocalDate.now().minusDays(1));
        operatesFromDateField.setPlaceholder(VALUE_FIELD_PLACEHOLDER);

        Binder<SingleValueBeen<Long>> binderOperatesFrom = new Binder<>();
        binderOperatesFrom.forField(operatesFromDateField)
                          .asRequired("Every hotel must operates from a certain date")
                          .withValidator(new DateRangeValidator("Date must be in the past",
                                  null, LocalDate.now().minusDays(1)))
                          .withConverter(LocalDate::toEpochDay, LocalDate::ofEpochDay,
                                  "Don't look like a date")
                          .bind(SingleValueBeen::getValue, SingleValueBeen::setValue);

        takeFieldValueAs = new TakeFieldValueAs(Long.class, binderOperatesFrom,
                LocalDate.class, operatesFromDateField);
        // save "operatesFrom" configuration
        hotelFieldsBindingRules.put("operatesFrom", takeFieldValueAs);

        // configure "category" field
        description = "Chose definite hotel category from the list";       // Required field
        NativeSelect<Category> categoryNativeSelect = new NativeSelect<>();
        categoryNativeSelect.setDescription(description);
        categoryNativeSelect.setEmptySelectionAllowed(false);           // NO emptySelect !!!
        categoryNativeSelect.setRequiredIndicatorVisible(true);         // Required field

        Binder<SingleValueBeen<Category>> binderCategory = new Binder<>();

        binderCategory.forField(categoryNativeSelect)
                .asRequired("Every hotel must have a category")
                .withValidator(category ->
                                hotelView.isCategoryNameInList(category == null
                                        ? ""
                                        : category.getName()),
                        "Define category, please. Maybe someone has already deleted this. Press Cancel to refresh list")
                .bind(SingleValueBeen::getValue, SingleValueBeen::setValue);

        takeFieldValueAs = new TakeFieldValueAs(Category.class, binderCategory,
                Category.class, categoryNativeSelect);
        // save "category" configuration
        hotelFieldsBindingRules.put("category", takeFieldValueAs);
        // set items
        this.updateCategoryNativeSelectItems();
        categoryNativeSelect.setItemCaptionGenerator(Category::getName);

        // configure "url" field
        description = "Link to the booking.com";       // Required field
        TextField urlTextField = vaadinComponentUtil.getStandardTextField(null,
                description, true);
        Binder<SingleValueBeen<String>> binderURL = new Binder<>();
        binderURL.readBean(new SingleValueBeen<>(null));    // new been with [null] value by default
        binderURL.forField(urlTextField)
                 .asRequired("Every hotel must have a link to booking.com")
                 .bind(SingleValueBeen::getValue, SingleValueBeen::setValue);

        takeFieldValueAs = new TakeFieldValueAs(String.class, binderURL,
                String.class, urlTextField);
        // save "url" configuration
        hotelFieldsBindingRules.put("url", takeFieldValueAs);

        // configure "description" field
        description = "Description of the hotel";       // NOT Required field
        TextArea descriptionTextArea = vaadinComponentUtil.getStandardTextArea(null,
                description, false);

        Binder<SingleValueBeen<String>> binderDescription = new Binder<>();
        binderDescription.forField(descriptionTextArea)
                         .bind(SingleValueBeen::getValue, SingleValueBeen::setValue);

        takeFieldValueAs = new TakeFieldValueAs(String.class, binderDescription,
                String.class, descriptionTextArea);
        // save "description" configuration
        hotelFieldsBindingRules.put("description", takeFieldValueAs);
        // reed new been with [null] value by default
        binderDescription.readBean(new SingleValueBeen<>(null));

        // put identical configurations
        hotelFieldsBindingRules.forEach((key, value) -> {
            if (value.getFieldValueTaker() instanceof TextField ||
                    value.getFieldValueTaker() instanceof TextArea) {
                // TextField or TextArea
                AbstractTextField textField = (AbstractTextField) value.getFieldValueTaker();
                textField.setPlaceholder(VALUE_FIELD_PLACEHOLDER);
            }
        });
    }
    
    public void updateCategoryNativeSelectItems(){

        try {
            ((NativeSelect<Category>) hotelFieldsBindingRules.get("category").getFieldValueTaker())
                    .setItems(categoryService.getAll());
        } catch (Exception exp) {
            LOGGER.log(Level.WARNING, "Can't take from DB all categories", exp);
        }
    }

}
