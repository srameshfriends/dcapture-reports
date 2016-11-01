package excel.accounting.ui;

/**
 * Style Builder
 */
public class StyleBuilder {
    private StringBuilder builder;

    public StyleBuilder() {
        builder = new StringBuilder();
    }

    public void border(int insert, double width, String style) {
        borderInsert(insert);
        borderWidth(width);
        borderStyle(style);
    }

    private void borderInsert(int value) {
        builder.append("-fx-border-insets:").append(value).append(";\n");
    }

    private void borderWidth(double width) {
        builder.append("-fx-border-width:").append(width).append(";\n");
    }

    private void borderStyle(String borderStyle) {
        builder.append("-fx-border-style:").append(borderStyle).append(";\n");
    }

    public void padding(int value) {
        builder.append("-fx-padding:").append(value).append("px;\n");
    }

    public void fontSize(int size) {
        builder.append("-fx-font-size:").append(size).append("pt;\n");
    }

    public void color(String colorCode) {
        builder.append("-fx-text-fill:").append(colorCode).append(";\n");
    }

    @Override
    public String toString() {
        return builder.toString();
    }
}
