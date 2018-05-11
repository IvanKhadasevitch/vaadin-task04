package views.categoryveiw;

import com.vaadin.data.Binder;
import com.vaadin.data.BinderValidationStatus;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import entities.Category;
import services.ICategoryService;
import utils.GetBeenFromSpringContext;
import utils.entitydescription.IVaadinComponentUtil;

import java.util.logging.Level;
import java.util.logging.Logger;

public class CategoryEditForm extends FormLayout {
    private static final Logger LOGGER = Logger.getLogger(CategoryEditForm.class.getName());

    private CategoryView categoryView;

    private ICategoryService categoryService;
    private IVaadinComponentUtil vaadinComponentUtil;

    private Category category;
    private final Binder<Category> categoryBinder = new Binder<>(Category.class);

    private TextField nameTextField;

    private Button saveCategoryBtn;
    private Button closeFormBtn;

    public CategoryEditForm(CategoryView categoryView){
        this.categoryView = categoryView;

        // get beans with names: categoryService,  vaadinComponentUtil
        this.categoryService = GetBeenFromSpringContext.getBeen(ICategoryService.class);
        this.vaadinComponentUtil = GetBeenFromSpringContext.getBeen(IVaadinComponentUtil.class);

        configureComponents();
        buildLayout();
    }

    private void configureComponents() {
        String caption = "Category name:";
        String description = "Hotels category name";                // Required field
        nameTextField = vaadinComponentUtil.getStandardTextField(caption, description, true);

        // connect entity fields with form fields
        categoryBinder.forField(nameTextField)
                      .asRequired("Every category must have name")
                      .bind(Category::getName, Category::setName);

        // buttons
        caption = "Save";
        description = "Save data in data base";;
        saveCategoryBtn = vaadinComponentUtil.getStandardFriendlyButton(caption, description);
        saveCategoryBtn.addClickListener(e -> saveCategory());

        caption = "Close";
        description = "Close without saving changes.";
        closeFormBtn = vaadinComponentUtil.getStandardPrimaryButton(caption, description);
        closeFormBtn.addClickListener(e -> closeCategoryEditForm());
    }

    private void buildLayout() {
        this.setMargin(true);       // Enable layout margins. Affects all four sides of the layout
        this.setVisible(false);

        // form tools - buttons
        HorizontalLayout buttons = new HorizontalLayout(saveCategoryBtn, closeFormBtn);
        buttons.setSpacing(true);

        // collect form components - form fields & buttons
        this.addComponents(nameTextField, buttons);
    }

    public void saveCategory() {
        // This will make all current validation errors visible
        BinderValidationStatus<Category> status = categoryBinder.validate();
        if (status.hasErrors()) {
            Notification.show("Validation error count: "
                    + status.getValidationErrors().size(), Notification.Type.WARNING_MESSAGE);
        }

        // save validated Category with not empty fields
        if ( !status.hasErrors() ) {
            // take validated data fields from binder to persisted category
            categoryBinder.writeBeanIfValid(this.category);

            // try save in DB new or update persisted category
            boolean isSaved = false;
            try {
                isSaved = categoryService.save(this.category) != null;
            } catch (Exception exp) {
                LOGGER.log(Level.WARNING, "Can't save category: " + this.category, exp);
            }

            if (isSaved) {
                // update category view
                categoryView.updateCategoryItems();
                this.setVisible(false);
                // update category Items in: CategoryView, HotelEditForm, HotelBulkUpdate
                this.categoryView.updateCategoryItems();

                Notification.show("Saved category with name: " + this.category.getName(),
                        Notification.Type.HUMANIZED_MESSAGE);
            } else {
                Notification.show( String.format("Can't save category with name [%s].Maybe someone has already changed it. Close form & try again ",
                        this.category.getName()), Notification.Type.ERROR_MESSAGE);
            }
        }
        categoryView.getAddCategoryBtn().setEnabled(true);
    }

    public void setCategory(Category category) {
        this.setVisible(true);

        // save persisted category in CategoryEditForm class
        this.category = category;

        // connect entity fields with form fields
        categoryBinder.readBean(category);
    }

    public void closeCategoryEditForm() {
        this.setVisible(false);
        categoryView.getAddCategoryBtn().setEnabled(true);
        categoryView.getCategoryList().deselectAll();
        // refresh category items
        categoryView.updateCategoryItems();
    }
}
