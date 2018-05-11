package views;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.Page;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.Notification;
import com.vaadin.ui.VerticalLayout;
import org.springframework.beans.factory.annotation.Autowired;
import ui.MainViewDisplay;
import ui.NavigationUI;

import javax.annotation.PostConstruct;

@UIScope
@SpringView(name = DefaultView.VIEW_NAME)
public class DefaultView extends VerticalLayout implements View {
    public static final String VIEW_NAME = "";
    private static final String TITLE_NAME = "";

    @Autowired
    private MainViewDisplay mainViewDisplay;

    @PostConstruct
    void init() {
        // use this method to determine view if Spring will create been
        // if we created bean with our own hands & @PostConstruct don't work

    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        // set page URI in browser history. Take URI from startPage and add VIEW_NAME
        NavigationUI.startPage.pushState(DefaultView.VIEW_NAME);

        // set Page Title
        Page.getCurrent().setTitle(NavigationUI.MAIN_TITLE + " " + TITLE_NAME);

        // This view is constructed NOT in the init() method(), but constructor
        mainViewDisplay.showView(this);
        Notification.show("Choose any menu item, please.", Notification.Type.HUMANIZED_MESSAGE);
    }

}
