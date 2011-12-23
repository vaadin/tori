package org.vaadin.tori.component;

@SuppressWarnings("serial")
public class ReplyComponent extends AuthoringComponent {

    public interface ReplyListener extends AuthoringListener {
    }

    public ReplyComponent(final ReplyListener listener,
            final String formattingSyntaxXhtml, final String caption) {
        super(listener, formattingSyntaxXhtml, caption);
    }

    public ReplyComponent(final ReplyListener listener,
            final String formattingSyntaxXhtml, final String caption,
            final String inputPrompt) {
        super(listener, formattingSyntaxXhtml, caption, inputPrompt);
    }

    @Override
    public void setCompactMode(final boolean compact) {
        super.setCompactMode(compact);
    }

    @Override
    public void setCollapsible(final boolean collapsible) {
        super.setCollapsible(collapsible);
    }
}
