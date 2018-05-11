package utils;

import com.vaadin.ui.Notification;
import org.springframework.beans.BeansException;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GetBeenFromSpringContext {
    private static final Logger LOGGER = Logger.getLogger(GetBeenFromSpringContext.class.getName());

    public static <T> T getBeen(Class<T> beenInterfaceClass) {
        WebApplicationContext context = ContextLoader.getCurrentWebApplicationContext();

        String beenName = beenInterfaceClass.getSimpleName();
        if (beenName.length() == 1) {
            beenName = beenName.toLowerCase();
        } else {
            String firstCharOfBeenName = beenName.substring(1,2).toLowerCase();
            beenName = firstCharOfBeenName + beenName.substring(2);
        }

        // delete after debug
        System.out.println("*******************************************");
//        Arrays.stream(context.getBeanDefinitionNames()).forEach(System.out::println);
        System.out.println("ask been from context: -> " + beenInterfaceClass);

        T been = null;
        try {
            been = context != null
                    ? (T) context.getBean(beenName)
                    : null;
        } catch (BeansException exeption) {
            String message = "In Spring context no been: " + beenInterfaceClass + "\n";
            LOGGER.log(Level.WARNING, message, exeption.getStackTrace());
            Notification.show(message, Notification.Type.ERROR_MESSAGE);
        }
        // delete after debug
        System.out.println("return been: " + been);
        System.out.println("*******************************************");

        return been;
    }
}
