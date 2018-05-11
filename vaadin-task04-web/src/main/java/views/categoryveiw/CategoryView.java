package views.categoryveiw;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.Page;
import com.vaadin.spring.access.ViewAccessControl;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import entities.Category;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import services.ICategoryService;
import ui.MainViewDisplay;
import ui.NavigationUI;
import ui.customcompanents.FilterWithClearBtn;
import ui.customcompanents.TopCenterComposite;
import utils.GetBeenFromSpringContext;
import utils.entitydescription.IVaadinComponentUtil;
import views.hotelveiw.HotelView;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;


@UIScope
@SpringView(name = CategoryView.VIEW_NAME)

//public class CategoryView extends VerticalLayout implements View {
public class CategoryView extends VerticalLayout implements View, ViewAccessControl {

    private static final Logger LOGGER = Logger.getLogger(CategoryView.class.getName());

    public static final String VIEW_NAME = "category";
    private static final String TITLE_NAME = "categories";

    private final String ERROR_NOTIFICATION = "Can't connect to data base. Try again or refer to administrator";

    @Autowired
    @Getter
    private HotelView hotelView;
    @Autowired
    private MainViewDisplay mainViewDisplay;
    @Autowired
    private IVaadinComponentUtil vaadinComponentUtil;

    private ICategoryService categoryService;

    private FilterWithClearBtn filterByName;
    @Getter
    private Button addCategoryBtn;
    private Button deleteCategoryBtn;
    private Button editCategoryBtn;

    @Getter
    final Grid<Category> categoryList = new Grid<>();

    private CategoryEditForm categoryEditForm = new CategoryEditForm(this);

    public CategoryView() {
        super();

        // del after debug
        System.out.println("start -> CategoryView.CONSTRUCTOR ");

        // take CategoryService been
        this.categoryService = GetBeenFromSpringContext.getBeen(ICategoryService.class);

        // del after debug
        System.out.println("getBeen(CategoryService.class): " + categoryService);
        System.out.println("STOP -> CategoryView.CONSTRUCTOR ");
    }

    @PostConstruct
    void init() {
        // use this method to determine view if Spring will create been
        // if we created bean with our own hands & @PostConstruct don't work

        // del after debug
        System.out.println("start -> CategoryView.init() ");

        // set view Configuration
        configureComponents();
        buildLayout();

        // del after debug
        System.out.println("STOP -> CategoryView.init() ");

    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        // This view is constructed in the init() method()

        // del after debug
        System.out.println("start -> CategoryView.enter() ");

        // refresh Grid state
        this.updateCategoryItems();

        // set page URI in browser history
        NavigationUI.startPage.pushState(VIEW_NAME);

        // set Page Title
        Page.getCurrent().setTitle(NavigationUI.MAIN_TITLE + " " + TITLE_NAME);

        mainViewDisplay.showView(this);

        // del after debug
        System.out.println("STOP -> CategoryView.enter()");
    }

    private void configureComponents() {
        // filterByName field with clear button
        filterByName = new FilterWithClearBtn("Filter by name...", e -> updateCategoryItems());

        // add Category Button
        String caption = "Add category";
        String description = "Open form to enter date and save new hotel category in data base";
        addCategoryBtn = vaadinComponentUtil.getStandardPrimaryButton(caption, description);
        addCategoryBtn.addClickListener(e -> {
            addCategoryBtn.setEnabled(false);
            // !!!!!!!!!!!!!!
            categoryEditForm.setCategory(new Category(null));
        } );

        // delete Hotel Button
        caption = "Delete category";
        description = "Delete all selected hotels categories from data base.";
        deleteCategoryBtn = vaadinComponentUtil.getStandardDangerButton(caption, description);
        deleteCategoryBtn.setEnabled(false);
        deleteCategoryBtn.addClickListener(e -> {
            // try delete selected items
            int deletedCategoriesCount = deleteCategories(categoryList.getSelectedItems());

            deleteCategoryBtn.setEnabled(false);
            addCategoryBtn.setEnabled(true);
            if (deletedCategoriesCount > 0) {
                // if any was deleted - update CategoryItems in:
                // CategoryView, HotelEditForm, HotelBulkUpdate
                updateCategoryItems();
            }

            Notification.show(String.format("Were deleted [%d] categories.", deletedCategoriesCount),
                    Notification.Type.WARNING_MESSAGE);
        });

        // edit Category Button (can edit only if one category was chosen)
        caption = "Edit category";
        description = "Open form to edit selected hotels category and save it in the data base.";
        editCategoryBtn = vaadinComponentUtil.getStandardPrimaryButton(caption, description);
        editCategoryBtn.setEnabled(false);
        editCategoryBtn.addClickListener(e -> {
            addCategoryBtn.setEnabled(true);       // switch on addNewCategory possibility
            Category editCandidate = categoryList.getSelectedItems().iterator().next();
            categoryEditForm.setCategory(editCandidate);
        });

        // Category list (Grid)
        categoryList.addColumn(Category::getName).setCaption("Name");
        categoryList.setSelectionMode(Grid.SelectionMode.MULTI);
        // delete and edit selected Category
        categoryList.addSelectionListener(e -> {
            // when Category is chosen - can delete or edit
            Set<Category> selectedCategories = e.getAllSelectedItems();
            if (selectedCategories != null && selectedCategories.size() == 1) {
                // chosen only one category - can add & delete & edit
                addCategoryBtn.setEnabled(true);
                deleteCategoryBtn.setEnabled(true);
                editCategoryBtn.setEnabled(true);
            } else if (selectedCategories != null && selectedCategories.size() > 1) {
                // chosen more then one category - can delete & add
                categoryEditForm.setVisible(false);
                addCategoryBtn.setEnabled(true);
                deleteCategoryBtn.setEnabled(true);
                editCategoryBtn.setEnabled(false);
            } else {
                // no any category chosen - can't delete & edit
                deleteCategoryBtn.setEnabled(false);
                editCategoryBtn.setEnabled(false);
                categoryEditForm.setVisible(false);
            }
        });

    }

    private void buildLayout() {
        Component[] controlComponents = {filterByName,
                addCategoryBtn, deleteCategoryBtn, editCategoryBtn};
        Component control = new TopCenterComposite(controlComponents);
        this.addComponent(control);
        this.setComponentAlignment(control, Alignment.TOP_CENTER);

        // content - categoryList & categoryEditForm
        Component[] categoryContentComponents = {categoryList, categoryEditForm};
        Component categoryContent = new TopCenterComposite(categoryContentComponents);
        this.addComponent(categoryContent);
        this.setComponentAlignment(categoryContent, Alignment.TOP_CENTER);

        // Compound view parts and allow resizing
        this.setSpacing(true);
        this.setMargin(false);
        this.setWidth("100%");
    }

    // this central place to update All other usages of Categories
    // (HotelEditForm & HotelBulkUpdate)
    public void updateCategoryItems() {
        // del after debug
        System.out.println("start -> CategoryView.updateCategoryItems() ");
        // update categoryGrid in CategoryView
        try {
            List<Category> categoryList = categoryService.getAllByFilter(filterByName.getValue());
            this.categoryList.setItems(categoryList);
        } catch(Exception exp) {
            LOGGER.log(Level.WARNING, "Can't take all categories from DB by filter: filterByName.getValue()", exp);
            Notification.show(ERROR_NOTIFICATION, Notification.Type.ERROR_MESSAGE);
        }
        // update categories usages in (HotelEditForm & HotelBulkUpdate)
        ((NavigationUI) UI.getCurrent()).getHotelView().getHotelEditForm().updateCategoryItems();
        ((NavigationUI) UI.getCurrent()).getHotelView().getHotelBulkUpdate().updateCategoryNativeSelectItems();

        // del after debug
        System.out.println("STOP -> CategoryView.updateCategoryItems()");
    }

    private int deleteCategories(Set<Category> categorySet) {
        int deleteCount = 0;
        Category categoryForDelete = null;
        try {
            for (Category category : categorySet) {
                categoryForDelete = category;
                categoryService.delete(category.getId());
                deleteCount++;
            }
        } catch(Exception exp) {
            LOGGER.log(Level.WARNING, "Can't delete category: " + categoryForDelete, exp);
            Notification.show(ERROR_NOTIFICATION, Notification.Type.ERROR_MESSAGE);
        }

        return deleteCount;
    }

    @Override
    public boolean isAccessGranted(UI ui, String s) {
        // del after debug
        System.out.println("start -> CategoryView.isAccessGranted ");

        //--------------- not use vile -------------

        // del after debug
        System.out.println("STOP -> CategoryView.isAccessGranted ");

        return true;
    }
}
