package excel.accounting.shared;

import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

/**
 * ViewController
 */
public class ViewManager {
    private ApplicationControl applicationControl;
    private Map<String, ViewHolder> viewRegister;
    private Map<String, Node> nodeRegister;
    private SplitPane basePanel;
    private VBox menuPanel;
    private ViewHolder currentView;
    private Stage primaryStage;

    private void initialize() {
        viewRegister = new HashMap<>();
        nodeRegister = new HashMap<>();
    }

    private Stage getPrimaryStage() {
        return primaryStage;
    }

    private ApplicationControl getApplicationControl() {
        return applicationControl;
    }

    private void onScreenResize(final Stage primaryStage, SplitPane basePanel, VBox menuPanel) {
        Screen screen = Screen.getPrimary();
        Rectangle2D bounds = screen.getVisualBounds();

        primaryStage.setX(bounds.getMinX() + 20);
        primaryStage.setY(bounds.getMinY() + 20);
        primaryStage.setWidth(bounds.getWidth() - 40);
        primaryStage.setHeight(bounds.getHeight() - 40);

        menuPanel.setMinWidth(260);
        menuPanel.setMaxWidth(360);
        menuPanel.setMinHeight(primaryStage.getHeight());
        basePanel.setPrefWidth(primaryStage.getWidth() - 268);
        basePanel.setPrefHeight(primaryStage.getHeight());
    }

    public void start(final Stage primaryStage, ApplicationControl control) {
        this.primaryStage = primaryStage;
        this.applicationControl = control;
        initialize();
        basePanel = new SplitPane();
        menuPanel = new VBox();
        onScreenResize(primaryStage, basePanel, menuPanel);
        basePanel.getItems().addAll(menuPanel, new Label("Loading...."));
        Scene scene = new Scene(new Group(basePanel), 1024, 768);
        scene.setFill(Color.GHOSTWHITE);
        primaryStage.setScene(scene);
        primaryStage.setTitle(control.getName());
        primaryStage.show();
    }

    private double getContentWidth() {
        return basePanel.getPrefWidth() - menuPanel.getPrefWidth() - 8;
    }

    private double getContentHeight() {
        return basePanel.getPrefHeight() - menuPanel.getPrefHeight() - 8;
    }

    public void addView(ViewHolder holder) {
        viewRegister.put(holder.getName(), holder);
        menuPanel.getChildren().add(createMenuLink(holder.getName(), holder.getTitle()));
    }

    private Hyperlink createMenuLink(final String name, String title) {
        Hyperlink hyperlink = new Hyperlink(title);
        hyperlink.setId(name);
        hyperlink.setOnAction(event -> showView(name));
        return hyperlink;
    }

    public void showView(String name) {
        ViewHolder holder = viewRegister.get(name);
        if (holder == null) {
            throw new NullPointerException("View is not registered " + name);
        }
        Node node = nodeRegister.get(name);
        if(node == null) {
            holder.setApplicationControl(getApplicationControl());
            holder.setStage(getPrimaryStage());
            node = holder.createControl();
            nodeRegister.put(name, node);
        }
        if (currentView != null && !currentView.canCloseView()) {
            return;
        }
        basePanel.getItems().remove(1);
        basePanel.getItems().add(node);
        holder.onResize(getContentWidth(), getContentHeight());
        currentView = holder;
    }

    public void closeAll() {
        viewRegister.values().forEach(ViewHolder::closeView);
    }
}
