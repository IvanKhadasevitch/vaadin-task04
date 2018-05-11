package ui;


import com.vaadin.annotations.Title;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.PushStateNavigation;
import com.vaadin.navigator.ViewDisplay;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinRequest;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.spring.annotation.*;
import com.vaadin.spring.navigator.SpringNavigator;
import com.vaadin.spring.navigator.SpringViewProvider;
import com.vaadin.spring.server.SpringVaadinServlet;
import com.vaadin.ui.*;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;
import views.DefaultView;
import views.categoryveiw.CategoryView;
import views.hotelveiw.HotelView;

import javax.servlet.annotation.WebListener;
import javax.servlet.annotation.WebServlet;
import java.util.Arrays;

@PushStateNavigation                    // allow separate URL with "/"
//@Title("vaadin: task04")
@Title(value = NavigationUI.MAIN_TITLE)
@UIScope
@SpringUI
public class NavigationUI extends UI {
    public static final String MAIN_TITLE = "vaadin: task04";

    public static String contextPath = null;
    public static Page startPage = null;

    @Autowired
    SpringViewProvider springViewProvider;

    @Autowired      // create been mainViewDisplay & set in Spring context
    private MainViewDisplay mainViewDisplay;
    @Autowired
    private MainMenu mainMenu;
//    @Autowired
//    private EntityUtil entityUtil;

    @Autowired
    private DefaultView defaultView;
    @Autowired
    private CategoryView categoryView;
    @Autowired
    @Getter
    private HotelView hotelView;

    public NavigationUI() {
        // delete after debug
        System.out.println("start -> NavigationUI. constructor");

        // -------- all work dane in the init method -----------

        // delete after debug
        System.out.println("STOP -> NavigationUI. constructor");
    }

    @Override
    protected void init(VaadinRequest request) {
        // delete after debug
        System.out.println("start -> NavigationUI.init(VaadinRequest)");

//        System.out.println("*******************************************");
//        WebApplicationContext context = ContextLoader.getCurrentWebApplicationContext();
//        Arrays.stream(context.getBeanDefinitionNames()).forEach(System.out::println);
//        System.out.println("*******************************************");

        // determine and set context path & start Page
        this.setContextPathAndStartPage(request);

        // delete after debug
        System.out.println("contextPath before pushState: " + contextPath);

        // scan event when URI changed by navigation in browser history
        getPage().addPopStateListener( e -> enter() );

        // set Navigator
        this.setNavigator(this.configureNavigator());

        // Read the initial URI fragment
        enter();

        // delete after debug
        System.out.println("STOP -> NavigationUI.init(VaadinRequest)");
    }

    private void enter() {
//        ... initialize the UI ...
// this method is coled every time, as browser history were manipulated
        configureComponents();
        buildLayout();

    }
    private Navigator configureNavigator() {
        Navigator navigator = new Navigator(this, (ViewDisplay) mainViewDisplay);

        // add SpringViewProvider
        navigator.addProvider(springViewProvider);

        // set AccessDenied ViewClass, as DefaultView
        springViewProvider.setAccessDeniedViewClass(DefaultView.class);

        // set Error View, as DefaultView
        navigator.setErrorView(defaultView);

        // register navigation views. As start -> send to defaultView
        navigator.addView(contextPath, defaultView);
        navigator.addView(contextPath + DefaultView.VIEW_NAME, defaultView);
        navigator.addView(contextPath + CategoryView.VIEW_NAME, categoryView);
        navigator.addView(contextPath + HotelView.VIEW_NAME, hotelView);

        return navigator;
    }
    private void configureComponents() {
        // delete after debug
        System.out.println("start -> NavigationUI.configureComponents");

        // clear all selections in mainMenu
        mainMenu.clearSelections();

        // delete after debug
        System.out.println("STOP -> NavigationUI.configureComponents");
    }

    private void buildLayout() {
        final VerticalLayout root = new VerticalLayout();
        root.setSizeFull();
        root.setMargin(new MarginInfo(true, true, true, true));
        setContent(root);

        // sen navigation bar - mainMenu
        root.addComponent(mainMenu);

        // mainViewDisplay this is Panel
        mainViewDisplay.setSizeFull();
        root.addComponent(mainViewDisplay);
        root.setExpandRatio(mainViewDisplay, 1.0f);
    }

    private void setContextPathAndStartPage(VaadinRequest request) {
        // determine and set context path & start Page
        if (contextPath == null) {
            contextPath = request.getContextPath();
            if (contextPath != null && !contextPath.isEmpty()) {
                // take contextPath without "/"
                contextPath = contextPath.substring(1) + "/";
            } else {
                // use root contextPath
                contextPath = "";
            }
            startPage = Page.getCurrent();
        }
    }

    // configuration to use Spring
    @Configuration
    @EnableVaadin        // add vaadin configuration to create beans needed for vaadin in Spring context
    public static class MyConfiguration {
    }

    @WebListener        // to start Spring context - register
    public static class MyContextLoaderListener extends ContextLoaderListener {
    }

    @WebServlet(urlPatterns = "/*", name = "MyUIServlet", asyncSupported = true)
    public static class MyUIServlet extends SpringVaadinServlet {
    }


}
