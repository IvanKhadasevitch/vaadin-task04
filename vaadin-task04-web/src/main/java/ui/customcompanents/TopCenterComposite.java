package ui.customcompanents;

import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;

public class TopCenterComposite extends CustomComponent {
    public TopCenterComposite(Component[] components) {
        HorizontalLayout layout = new HorizontalLayout(components);
        layout.setMargin(false);
        this.setSizeUndefined();
        this.setCompositionRoot(layout);
    }
}
