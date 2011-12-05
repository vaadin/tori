package org.vaadin.tori.component;

@SuppressWarnings("serial")
public class NewThreadComponent extends AuthoringComponent {

    public interface NewThreadListener extends AuthoringListener {
    }

    public NewThreadComponent(final NewThreadListener listener,
            final String formattingSyntaxXhtml) {
        super(listener, formattingSyntaxXhtml, "Thread body");
    }
}
