package ui;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.themes.ValoTheme;
import views.categoryveiw.CategoryView;
import views.hotelveiw.HotelView;

import java.util.List;

@SpringComponent                // allow Spring make a been (vaadin annotation)
@UIScope                        // mast be same as NavigationUI scope
public class MainMenu extends CustomComponent {
    private MenuBar.MenuItem previous = null;
    private final Label selection = new Label("-");
    private final MenuBar menuBar = new MenuBar();

    public MainMenu() {
        HorizontalLayout layout = new HorizontalLayout();

        menuBar.setStyleName(ValoTheme.MENUBAR_BORDERLESS);
        layout.addComponent(menuBar);
        // A feedback component
        layout.addComponent(selection);

        // put menu items
        menuBar.addItem("Hotel", VaadinIcons.BUILDING, hotelBtn());
        menuBar.addItem("Category", VaadinIcons.ACADEMY_CAP, categoryBtn());

        layout.setSizeUndefined();
        setCompositionRoot(layout);
    }

    private MenuBar.Command hotelBtn() {

        return new MenuBar.Command() {
            @Override
            public void menuSelected(MenuBar.MenuItem selectedItem) {
                highLightSelection(selection, selectedItem);
//                getUI().getNavigator().navigateTo(HotelView.VIEW_NAME);
                getUI().getNavigator().navigateTo(NavigationUI.contextPath + HotelView.VIEW_NAME);
            }
        };
    }

    private MenuBar.Command categoryBtn() {

        return new MenuBar.Command() {
            @Override
            public void menuSelected(MenuBar.MenuItem selectedItem) {
                highLightSelection(selection, selectedItem);
//                getUI().getNavigator().navigateTo(CategoryView.VIEW_NAME);
                getUI().getNavigator().navigateTo(NavigationUI.contextPath + CategoryView.VIEW_NAME);
            }
        };
    }

    private void highLightSelection(Label selection, MenuBar.MenuItem selectedItem) {
        selection.setValue("Ordered a " +
                selectedItem.getText() +
                " from menu.");

        if (previous != null)
            previous.setStyleName("unchecked");

        selectedItem.setStyleName("checked");
        previous = selectedItem;
    }

    public void clearSelections() {
        List<MenuBar.MenuItem> menuItems = menuBar.getItems();
        for (MenuBar.MenuItem item : menuItems) {
            item.setStyleName("unchecked");
        }
        selection.setValue("-");
    }
}
