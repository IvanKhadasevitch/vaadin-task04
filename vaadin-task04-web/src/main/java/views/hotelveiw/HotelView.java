package views.hotelveiw;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.Page;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.*;
import com.vaadin.ui.renderers.HtmlRenderer;
import com.vaadin.ui.themes.ValoTheme;
import entities.Category;
import entities.Hotel;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import services.ICategoryService;
import services.IHotelService;
import ui.MainViewDisplay;
import ui.NavigationUI;
import ui.customcompanents.FilterWithClearBtn;
import ui.customcompanents.TopCenterComposite;
import utils.GetBeenFromSpringContext;
import utils.entitydescription.IVaadinComponentUtil;
import utils.entitydescription.vo.EntityFieldDescription;
import utils.entitydescription.IEntityUtil;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

@UIScope
@SpringView(name = HotelView.VIEW_NAME)
public class HotelView extends VerticalLayout implements View {
    private static final Logger LOGGER = Logger.getLogger(HotelView.class.getName());

    public static final String VIEW_NAME = "hotel";
    private static final String TITLE_NAME = "hotels";

    @Autowired
    private MainViewDisplay mainViewDisplay;
    @Autowired
    private IEntityUtil entityUtil;
    @Autowired
    private IVaadinComponentUtil vaadinComponentUtil;

    private IHotelService hotelService;
    private ICategoryService categoryService;

    private FilterWithClearBtn filterByName;
    private FilterWithClearBtn filterByAddress;
    @Getter
    private Button addHotelBtn;
    private Button deleteHotelBtn;
    private Button editHotelBtn;
    private Button bulkUpdateBtn;

    @Getter
    final Grid<Hotel> hotelGrid = new Grid<>();
    @Getter
    private HotelEditForm hotelEditForm;
    @Getter
    private HotelBulkUpdate hotelBulkUpdate;

//    private NativeSelect<Category> categoryNativeSelect;

    public HotelView() {
        super();

        // del after debug
        System.out.println("start -> HotelView.CONSTRUCTOR ");

        // take beans ICategoryService & IHotelService
        this.hotelService = GetBeenFromSpringContext.getBeen(IHotelService.class);
        this.categoryService = GetBeenFromSpringContext.getBeen(ICategoryService.class);

        // create HotelEditForm & HotelBulkUpdate
        this.hotelEditForm = new HotelEditForm(this);
        this.hotelBulkUpdate = new HotelBulkUpdate(this);

        // del after debug
        System.out.println("getBeen(IHotelService.class): " + hotelService);
        System.out.println("STOP -> HotelView.CONSTRUCTOR ");
    }

    @PostConstruct
    void init() {
        // use this method to determine view if Spring will create been
        // if we created bean with our own hands & @PostConstruct don't work

        // del after debug
        System.out.println("start -> HotelView.init() ");

        // set view Configuration
        configureHotelEntity();
        configureComponents();
        buildLayout();

        // del after debug
        System.out.println("STOP -> HotelView.init() ");
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        // This view is constructed in the init() method()

        // del after debug
        System.out.println("start -> HotelView.enter() ");

        // take fresh data from DB
        this.updateHotelList();

        // set page URI in browser history
        NavigationUI.startPage.pushState(VIEW_NAME);

        // set Page Title
        Page.getCurrent().setTitle(NavigationUI.MAIN_TITLE + " " + TITLE_NAME);

        mainViewDisplay.showView(this);

        // del after debug
        System.out.println("STOP -> HotelView.enter()");
    }

    private void configureComponents() {
        // filter fields with clear button
        filterByName = new FilterWithClearBtn("Filter by name...",
                e -> updateHotelList());
        filterByAddress = new FilterWithClearBtn("Filter by address...",
                e -> updateHotelList());

        // add Hotel Button
        String caption = "Add hotel";
        String description = "Open form to enter date and save new hotel in data base";
        addHotelBtn = vaadinComponentUtil.getStandardPrimaryButton(caption, description);
        addHotelBtn.addClickListener(e -> {
            addHotelBtn.setEnabled(false);
            hotelEditForm.setHotel(new Hotel());
        });

        // delete Hotel Button
        caption = "Delete hotel";
        description = "Delete all selected hotels from data base.";
        deleteHotelBtn = vaadinComponentUtil.getStandardDangerButton(caption, description);
        deleteHotelBtn.setEnabled(false);
        deleteHotelBtn.addClickListener(e -> {
            int deletedHotelsCount = deleteSelectedHotels(hotelGrid.getSelectedItems());

            deleteHotelBtn.setEnabled(false);
            addHotelBtn.setEnabled(true);
            updateHotelList();
            Notification.show(String.format("Were deleted [%d] hotels.", deletedHotelsCount),
                    Notification.Type.WARNING_MESSAGE);
        });

        // edit Hotel Button (can edit only if one hotel was chosen)
        caption = "Edit hotel";
        description = "Open form to edit selected hotel and save it in the data base.";
        editHotelBtn = vaadinComponentUtil.getStandardPrimaryButton(caption, description);
        editHotelBtn.setEnabled(false);
        editHotelBtn.addClickListener(e -> {
            addHotelBtn.setEnabled(true);       // switch on addNewHotel possibility
            Hotel editCandidate = hotelGrid.getSelectedItems().iterator().next();
            hotelEditForm.setHotel(editCandidate);
        });

        // bulk update Button
        caption = "Bulk update";
        description = "Opens a form for changing and saving in the DB the values of all the selected elements of the column to the same value";
        bulkUpdateBtn = vaadinComponentUtil.getStandardPrimaryButton(caption, description);
        bulkUpdateBtn.setEnabled(false);
        bulkUpdateBtn.addClickListener(e -> {
            bulkUpdateBtn.setEnabled(true);
            hotelBulkUpdate.getPopup().setPopupVisible(true);
        });

        // Hotel list (Grid)
        hotelGrid.addColumn(Hotel::getName)
                 .setCaption(getFieldCaption("name"));    // setCaption("Name")
        hotelGrid.setFrozenColumnCount(1);                        // froze "name" column
        hotelGrid.addColumn(Hotel::getAddress)
                 .setCaption(getFieldCaption("address"))  // setCaption("Address")
                 .setHidable(true);
        hotelGrid.addColumn(Hotel::getRating)
                 .setCaption(getFieldCaption("rating"))     // setCaption("Rating")
                 .setHidable(true);
        hotelGrid.addColumn(hotel -> LocalDate.ofEpochDay(hotel.getOperatesFrom()))
                 .setCaption(getFieldCaption("operatesFrom"))   // setCaption("Operates from")
                 .setHidable(true);
        ;
        hotelGrid.addColumn(hotel -> {
                    String categoryName = hotel.getCategory() != null
                            ? hotel.getCategory().getName()
                            : "";
                    return this.existWithName(categoryName);
                })
                 .setCaption(getFieldCaption("category"))   // setCaption("Category")
                 .setHidable(true);


        hotelGrid.addColumn(hotel ->
                        "<a href='" + hotel.getUrl() + "' target='_blank'>more info</a>",
                new HtmlRenderer())
                 .setCaption(getFieldCaption("url"))        // setCaption("URL")
                 .setHidable(true);

        hotelGrid.addColumn(Hotel::getDescription)
                 .setCaption(getFieldCaption("description"))    // setCaption("Description")
                 .setHidable(true);

        hotelGrid.setSelectionMode(Grid.SelectionMode.MULTI);      // MULTI select possible !!!
        // delete and edit selected Hotel
        hotelGrid.addSelectionListener(e -> {
            // when Hotel is chosen - can delete or edit
            Set<Hotel> selectedHotels = e.getAllSelectedItems();
            if (selectedHotels != null && selectedHotels.size() == 1) {
                // chosen only one hotel - can add & delete & edit, can't bulkUpdate
                addHotelBtn.setEnabled(true);
                deleteHotelBtn.setEnabled(true);
                editHotelBtn.setEnabled(true);
                bulkUpdateBtn.setEnabled(false);
            } else if (selectedHotels != null && selectedHotels.size() > 1) {
                // chosen more then one hotel - can delete & add & bulk update
                hotelEditForm.setVisible(false);
                addHotelBtn.setEnabled(true);
                deleteHotelBtn.setEnabled(true);
                bulkUpdateBtn.setEnabled(true);
                editHotelBtn.setEnabled(false);
            } else {
                // no any hotel chosen - can't delete & edit & bulkUpdate
                deleteHotelBtn.setEnabled(false);
                editHotelBtn.setEnabled(false);
                bulkUpdateBtn.setEnabled(false);
                hotelEditForm.setVisible(false);
            }
        });
    }

    private void buildLayout() {
        // tools bar - filters & buttons
        HorizontalLayout control = new HorizontalLayout(filterByName, filterByAddress,
                addHotelBtn, deleteHotelBtn, editHotelBtn, bulkUpdateBtn);

        control.setMargin(false);
        control.setWidth("100%");
        // divide free space between filterByName (50%) & filterByAddress (50%)
        control.setExpandRatio(filterByName, 1);
        control.setExpandRatio(filterByAddress, 1);

        // bulk update component
        Component[] bulkUpdateComponents = {hotelBulkUpdate.getPopup()};
        Component bulkUpdateComposite = new TopCenterComposite(bulkUpdateComponents);

        // content - HotelList & hotelEditForm
        HorizontalLayout hotelContent = new HorizontalLayout(hotelGrid, hotelEditForm);
        hotelGrid.setSizeFull();            // size 100% x 100%
        hotelGrid.addStyleName(ValoTheme.TABLE_SMALL);
        hotelEditForm.setSizeFull();
        hotelContent.setMargin(false);
        hotelContent.setWidth("100%");
        hotelContent.setHeight(31, Unit.REM);
        hotelContent.setExpandRatio(hotelGrid, 229);
        hotelContent.setExpandRatio(hotelEditForm, 92);

        // Compound view parts and allow resizing
        this.addComponents(control, hotelContent, bulkUpdateComposite);
        this.setComponentAlignment(bulkUpdateComposite, Alignment.TOP_CENTER);


        this.setSpacing(true);
        this.setMargin(false);
        this.setWidth("100%");
    }

    public void updateHotelList() {
        try {
            List<Hotel> hotelList = hotelService.getAllByFilter(filterByName.getValue(),
                    filterByAddress.getValue());
            // sate fresh hotels from Db
            this.hotelGrid.setItems(hotelList);
            // sate fresh categories from DB
            this.hotelEditForm.updateCategoryItems();
        } catch (Exception exp) {
            LOGGER.log(Level.WARNING,
                    String.format("Can't take from DB all hotels by filters: name contains [%s] & address contains [%s]",
                            filterByName.getValue(), filterByAddress.getValue()), exp);
        }

    }

//    public void updateCategoryNativeSelectItems() {
//        try {
//            this.categoryNativeSelect.setItems(categoryService.getAll());
//        } catch (Exception exp) {
//            LOGGER.log(Level.WARNING, "Can't take from DB all categories", exp);
//        }
//    }

    public boolean isCategoryNameInList(String categoryName) {
        boolean result = false;
        try {
            result = categoryService.existWithName(categoryName);
        } catch (Exception exp) {
            LOGGER.log(Level.WARNING, "Can't take from DB category with name: " + categoryName, exp);
        }

        return result;
    }

    private String existWithName(String categoryName) {
        try {
            return categoryService.existWithName(categoryName)
                    ? categoryName
                    : Category.NULL_CATEGORY_REPRESENTATION;
        } catch (Exception exp) {
            LOGGER.log(Level.WARNING, "Can't take from DB category by name: " + categoryName, exp);

            return Category.NULL_CATEGORY_REPRESENTATION;
        }
    }

    private int deleteSelectedHotels(Set<Hotel> selectedHotels) {
        int count = 0;
        Hotel hotelForeDelete = null;
        try {
            for (Hotel hotel : selectedHotels) {
                hotelForeDelete = hotel;
                hotelService.delete(hotel.getId());
                count++;
            }
        } catch (Exception exp) {
            LOGGER.log(Level.WARNING, "Can't delete from DB hotel: " + hotelForeDelete, exp);
            Notification.show("Connection with DB was lost while deleting.", Notification.Type.ERROR_MESSAGE);
        }

        return count;
    }



    private void configureHotelEntity() {
        Map<String, EntityFieldDescription> hotelEntityDescription = entityUtil.getEntityDescription(Hotel.class);

        // configure "operatesFrom" field caption
        hotelEntityDescription.get("operatesFrom").setFieldCaption("Operates from");

        // configure "url" field caption
        hotelEntityDescription.get("url").setFieldCaption("URL");
    }

    private String getFieldCaption(String fieldName) {
        Map<String, EntityFieldDescription> hotelEntityDescription = entityUtil.getEntityDescription(Hotel.class);
        EntityFieldDescription entityFieldDescription = hotelEntityDescription.get(fieldName);
        return entityFieldDescription.getFieldCaption();
    }
}
