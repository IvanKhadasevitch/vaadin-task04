package views.hotelveiw;

import com.vaadin.data.Binder;
import com.vaadin.data.BinderValidationStatus;
import com.vaadin.data.converter.StringToIntegerConverter;
import com.vaadin.data.validator.DateRangeValidator;
import com.vaadin.server.SerializableFunction;
import com.vaadin.server.SerializablePredicate;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import entities.Category;
import entities.Hotel;
import services.ICategoryService;
import services.IHotelService;
import utils.GetBeenFromSpringContext;
import utils.entitydescription.IVaadinComponentUtil;

import java.time.LocalDate;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class HotelEditForm extends FormLayout {
    private static final Logger LOGGER = Logger.getLogger(HotelEditForm.class.getName());

    private HotelView hotelView;

    private IHotelService hotelService;
    private ICategoryService categoryService;
    private IVaadinComponentUtil vaadinComponentUtil;

    private Hotel hotel = new Hotel();
    private Binder<Hotel> hotelBinder = new Binder<>(Hotel.class);

    private TextField nameTextField;
    private TextField addressTextField;
    private TextField ratingTextField;
    private DateField operatesFromDateField;
    private NativeSelect<String> categoryNativeSelect = new NativeSelect<>("Category:");
    private TextField urlTextField;
    private TextArea descriptionTextArea;

    private Button saveHotelBtn;
    private Button closeFormBtn;

    public HotelEditForm(HotelView hotelView) {
        this.hotelView = hotelView;

        // take beans: ICategoryService, IHotelService, IVaadinComponentUtil
        this.hotelService = GetBeenFromSpringContext.getBeen(IHotelService.class);
        this.categoryService = GetBeenFromSpringContext.getBeen(ICategoryService.class);
        this.vaadinComponentUtil = GetBeenFromSpringContext.getBeen(IVaadinComponentUtil.class);

        configureComponents();
        buildLayout();
        // fill categories items
        updateCategoryItems();
    }

    private void configureComponents() {
        // add ToolTip to the forms fields & configure Components
        String caption = "Name:";
        String description = "Hotels name";
        nameTextField = vaadinComponentUtil.getStandardTextField(caption, description, true);

        caption = "Address:";
        description = "Hotels address";
        addressTextField = vaadinComponentUtil.getStandardTextField(caption, description, true);

        caption = "Rating:";
        description = "Hotels rating from 0 to 5 stars";
        ratingTextField = vaadinComponentUtil.getStandardTextField(caption, description, true);

        caption = "Operates from:";
        description = "Date of the beginning of the operating of the hotel must be in the past";
        operatesFromDateField = vaadinComponentUtil.getStandardDateField(caption, description, true);

        categoryNativeSelect.setDescription("Hotel category");
        categoryNativeSelect.addStyleName(ValoTheme.COMBOBOX_SMALL);
        categoryNativeSelect.setEmptySelectionAllowed(true);

        caption = "URL:";
        description = "Info about hotel on the booking.com";
        urlTextField = vaadinComponentUtil.getStandardTextField(caption, description, true);

        caption = "Description:";
        description = "Hotel description";
        // not required
        descriptionTextArea = vaadinComponentUtil.getStandardTextArea(caption, description, false);

        // connect entity fields with form fields by Binder
        hotelBinder.forField(nameTextField)
                   // Shorthand for requiring the field to be non-empty
                   .asRequired("Every hotel must have a name")
                   .bind(Hotel::getName, Hotel::setName);

        hotelBinder.forField(addressTextField)
                   .asRequired("Every hotel must have a address")
                   .bind(Hotel::getAddress, Hotel::setAddress);

        hotelBinder.forField(ratingTextField)
                   .asRequired("Every hotel must have a rating")
                   .withConverter(new StringToIntegerConverter("Enter an integer, please"))
                   .withValidator(rating -> rating >= 0 && rating <= 5,
                           "Rating must be between 0 and 5")
                   .bind(Hotel::getRating, Hotel::setRating);

        hotelBinder.forField(operatesFromDateField)
                   .asRequired("Every hotel must operates from a certain date")
                   .withValidator(new DateRangeValidator("Date must be in the past",
                           null, LocalDate.now().minusDays(1)))
                   .withConverter(LocalDate::toEpochDay, LocalDate::ofEpochDay,
                           "Don't look like a date")
                   .bind(Hotel::getOperatesFrom, Hotel::setOperatesFrom);

        SerializableFunction<String, Category> toModel = (this::getCategoryByName);
        SerializableFunction<Category, String> toPresentation = (category -> {
            return category != null ? category.getName() : "";
        });
        SerializablePredicate<? super String> isCategoryNameInList = (hotelView::isCategoryNameInList);
        hotelBinder.forField(categoryNativeSelect)
                   .asRequired("Every hotel must have a category")
                   .withValidator(isCategoryNameInList, "Define category, please")
                   .withConverter(toModel, toPresentation, "No such category")
                   .bind(Hotel::getCategory, Hotel::setCategory);

        urlTextField.setRequiredIndicatorVisible(true);
        hotelBinder.forField(urlTextField)
                   .asRequired("Every hotel must have a link to booking.com")
                   .bind(Hotel::getUrl, Hotel::setUrl);

        hotelBinder.forField(descriptionTextArea).bind(Hotel::getDescription, Hotel::setDescription);

        // buttons
        caption = "Save";
        description = "Save data in data base";
        saveHotelBtn = vaadinComponentUtil.getStandardFriendlyButton(caption, description);
        saveHotelBtn.addClickListener(e -> saveHotel());

        caption = "Close";
        description = "Close without saving changes.";
        closeFormBtn = vaadinComponentUtil.getStandardPrimaryButton(caption, description);
        closeFormBtn.addClickListener(e -> closeHotelEditForm());
    }

    private void buildLayout() {
        this.setMargin(true);       // Enable layout margins. Affects all four sides of the layout
        this.setVisible(false);     // hide form at start

        HorizontalLayout buttons = new HorizontalLayout(saveHotelBtn, closeFormBtn);
        buttons.setSpacing(true);

        this.addComponents(nameTextField, addressTextField, ratingTextField, operatesFromDateField, categoryNativeSelect,
                urlTextField, descriptionTextArea, buttons);
    }

    public void saveHotel() {
        // This will make all current validation errors visible
        BinderValidationStatus<Hotel> status = hotelBinder.validate();
        if (status.hasErrors()) {
            Notification.show("Validation error count: "
                    + status.getValidationErrors().size(), Notification.Type.WARNING_MESSAGE);
        }

        // save validated hotel with not empty fields (field "descriptionTextArea" - can be empty)
        if (!status.hasErrors()) {

            // !!!!!!!!!!!!
            // take validated data fields from binder to persisted categoryNativeSelect
            hotelBinder.writeBeanIfValid(this.hotel);
//            // try save in DB new or update persisted hotel
            boolean isSaved = false;
            try {
                isSaved = hotelService.save(this.hotel) != null;
            } catch (Exception exp) {
                LOGGER.log(Level.WARNING, "Can't save hotel: " + this.hotel, exp);
            }

            if (isSaved) {
                hotelView.updateHotelList();
                this.setVisible(false);

                hotelView.getAddHotelBtn().setEnabled(true);

                Notification.show("Saved hotel with name: " + hotel.getName(),
                        Notification.Type.HUMANIZED_MESSAGE);
            } else {
                Notification.show(String.format("Can't save hotel with name [%s]. Maybe someone has already changed it. Close form & try again ",
                        hotel.getName()), Notification.Type.ERROR_MESSAGE);
            }
        }
        // update Category Items from DB to pick up changes
        updateCategoryItems();
    }

    public void setHotel(Hotel hotel) {
        this.setVisible(true);
        this.hotel = hotel;

        // connect entity fields with form fields
        // !!!!!!!!!!!!-------------
        hotelBinder.readBean(hotel);

        // refresh categoryNativeSelect items
        updateCategoryItems();
        // set active categoryNativeSelect in categoryNativeSelect items list
        categoryNativeSelect.setValue(hotel.getCategory() != null ? hotel.getCategory().getName() : "");
    }

    public void closeHotelEditForm() {
        this.setVisible(false);
        hotelView.getAddHotelBtn().setEnabled(true);
        hotelView.getHotelGrid().deselectAll();
        hotelView.updateHotelList();
    }

    public void updateCategoryItems() {
        try {
            // fill categories items
            List<String> categoryItems = categoryService.getAll()
                                                        .stream()
                                                        .map(Category::getName)
                                                        .collect(Collectors.toList());
            categoryNativeSelect.setItems(categoryItems);
            categoryNativeSelect.setValue(hotel != null && hotel.getCategory() != null
                    ? hotel.getCategory().getName()
                    : "");
        } catch (Exception exp) {
            LOGGER.log(Level.WARNING, "Can't get all categories from Db", exp);
            Notification.show("Connection with DB were lost. Try again.",
                    Notification.Type.ERROR_MESSAGE);
        }

    }

    private Category getCategoryByName(String categoryName) {
        Category result = null;
        try {
            result = categoryService.getCategoryByName(categoryName);
        } catch (Exception exp) {
            LOGGER.log(Level.WARNING, "Can't get from Db category by name: " + categoryName, exp);
            Notification.show("Connection with DB were lost. Try again.",
                    Notification.Type.ERROR_MESSAGE);
        }

        return result;
    }
}
