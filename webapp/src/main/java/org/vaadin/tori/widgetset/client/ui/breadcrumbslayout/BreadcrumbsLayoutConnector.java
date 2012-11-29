package org.vaadin.tori.widgetset.client.ui.breadcrumbslayout;

import org.vaadin.tori.component.breadcrumbs.BreadcrumbsLayout;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.vaadin.client.ConnectorHierarchyChangeEvent;
import com.vaadin.client.ui.ManagedLayout;
import com.vaadin.client.ui.customlayout.CustomLayoutConnector;
import com.vaadin.client.ui.layout.ElementResizeEvent;
import com.vaadin.client.ui.layout.ElementResizeListener;
import com.vaadin.shared.ui.Connect;

@Connect(BreadcrumbsLayout.class)
public class BreadcrumbsLayoutConnector extends CustomLayoutConnector implements
        ManagedLayout {
    private static final long serialVersionUID = 6670085480076350493L;
    private final ElementResizeListener resizeListener = new ElementResizeListener() {
        @Override
        public void onElementResize(final ElementResizeEvent e) {
            makeSuretheCrumbsFit();
        }
    };

    @Override
    public void onConnectorHierarchyChange(
            final ConnectorHierarchyChangeEvent event) {
        super.onConnectorHierarchyChange(event);
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {
            @Override
            public void execute() {
                BreadcrumbsLayoutConnector.makeSuretheCrumbsFit();
            }
        });
    }

    @Override
    protected void init() {
        super.init();
        getLayoutManager().addElementResizeListener(getWidget().getElement(),
                resizeListener);
    }

    @Override
    public void onUnregister() {
        super.onUnregister();
        getLayoutManager().removeElementResizeListener(
                getWidget().getElement(), resizeListener);
    }

    private static native void makeSuretheCrumbsFit()
    /*-{
        $wnd.org_vaadin_tori_breadcrumbslayout_makesurethecrumbsfit();
    }-*/;
}
