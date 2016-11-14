package excel.accounting.ui;

/**
 * View Config
 *
 * @author Ramesh
 * @since Oct, 2016
 */
public class ViewConfig {
    private final String name, title;
    private final ViewGroup viewGroup;

    public ViewConfig(ViewGroup viewGroup, String name, String title) {
        this.viewGroup = viewGroup;
        this.name = name;
        this.title = title;
    }

    ViewGroup getViewGroup() {
        return viewGroup;
    }

    public String getName() {
        return name;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public String toString() {
        return name + " " + title + " " + viewGroup;
    }
}
