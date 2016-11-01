package excel.accounting.ui;

import excel.accounting.dialog.SessionDialog;
import excel.accounting.shared.ApplicationControl;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * ViewController
 */
public class ViewManager implements ActionHandler {
    private static final Logger logger = Logger.getLogger(ViewManager.class);
    private final int height = 768, menuWidth = 240, headerSpace = 44;
    private double contentWidth, contentHeight;
    private ApplicationControl applicationControl;
    private Map<ViewConfig, ViewHolder> viewRegister;
    private Map<String, Node> nodeRegister;
    private SplitPane basePanel;
    private BorderPane contentPane;
    private Label titleLabel;
    private TextArea messagePanel;
    private VBox menuPanel;
    private MenuGroupPanel registersPanel, expensePanel, incomePanel, assetsPanel, managementPanel;
    private SessionDialog sessionDialog;

    private ViewHolder currentView;
    private Stage primaryStage;

    private Stage getPrimaryStage() {
        return primaryStage;
    }

    private ApplicationControl getApplicationControl() {
        return applicationControl;
    }

    public void start(ApplicationControl control, Stage primaryStage) {
        final int width = 1024;
        viewRegister = new HashMap<>();
        nodeRegister = new HashMap<>();
        this.applicationControl = control;
        this.primaryStage = primaryStage;
        this.primaryStage.setWidth(width);
        this.primaryStage.setHeight(height);
        this.primaryStage.widthProperty().addListener((arg0, arg1, arg2) -> onWidthChanged((Double) arg2));
        this.primaryStage.heightProperty().addListener((arg0, arg1, arg2) -> onHeightChanged((Double) arg2));
        sessionDialog = new SessionDialog();
        sessionDialog.initialize(control, primaryStage);
        sessionDialog.setViewHandler(this);
        //
        StyleBuilder titleStyle = new StyleBuilder();
        titleStyle.fontSize(14);
        titleStyle.color("#0000ff");
        titleStyle.padding(2);
        //
        titleLabel = new Label();
        titleLabel.setStyle(titleStyle.toString());
        titleLabel.setMinHeight(30);
        titleLabel.setMaxHeight(30);
        messagePanel = createLogArea();
        applicationControl.setMessagePanel(messagePanel);
        //
        contentPane = new BorderPane();
        contentPane.setTop(titleLabel);
        contentPane.setCenter(new Label("Welcome to Excel Accounting"));
        contentPane.setBottom(messagePanel);
        //
        basePanel = new SplitPane();
        createMenuPanel();
        basePanel.getItems().addAll(menuPanel, contentPane);
        Scene scene = new Scene(new Group(basePanel), width, height);
        scene.setFill(Color.GHOSTWHITE);
        //
        primaryStage.setScene(scene);
        primaryStage.setTitle(control.getName());
        primaryStage.show();
        onWidthChanged(width);
        onHeightChanged(height);
    }

    private TextArea createLogArea() {
        StyleBuilder messageStyle = new StyleBuilder();
        messageStyle.fontSize(12);
        messageStyle.color("#ff0000");
        messageStyle.padding(2);
        TextArea textArea = new TextArea();
        textArea.setStyle(messageStyle.toString());
        textArea.setMaxHeight(30);
        textArea.setEditable(false);
        return textArea;
    }

    private void onWidthChanged(double width) {
        basePanel.setPrefWidth(width - 20);
        contentWidth = width - menuWidth;
        if (currentView != null) {
            currentView.onWidthChanged(contentWidth);
        }
    }

    private void onHeightChanged(double height) {
        contentHeight = height - headerSpace;
        basePanel.setPrefHeight(contentHeight);
        menuPanel.setPrefHeight(contentHeight);
        if (currentView != null) {
            currentView.onHeightChanged(contentHeight);
        }
    }

    private void createMenuPanel() {
        menuPanel = new VBox();
        menuPanel.setMaxWidth(menuWidth);
        menuPanel.setPrefHeight(height - headerSpace);
        //
        registersPanel = new MenuGroupPanel("Registers");
        expensePanel = new MenuGroupPanel("Expense");
        incomePanel = new MenuGroupPanel("Income");
        assetsPanel = new MenuGroupPanel("Assets");
        managementPanel = new MenuGroupPanel("Management");
        menuPanel.getChildren().addAll(incomePanel, expensePanel, assetsPanel, registersPanel, managementPanel);
    }

    public void addView(ViewHolder holder) {
        ViewConfig config = holder.getViewConfig();
        final ViewGroup viewGroup = config.getViewGroup();
        viewRegister.put(config, holder);
        if (ViewGroup.Expense.equals(viewGroup)) {
            expensePanel.addViewLink(config);
        } else if (ViewGroup.Income.equals(viewGroup)) {
            incomePanel.addViewLink(config);
        } else if (ViewGroup.Assets.equals(viewGroup)) {
            assetsPanel.addViewLink(config);
        } else if (ViewGroup.Registers.equals(viewGroup)) {
            registersPanel.addViewLink(config);
        } else if (ViewGroup.Management.equals(viewGroup)) {
            managementPanel.addViewLink(config);
        }
    }

    private ViewConfig getViewConfig(String name) {
        for (ViewConfig viewConfig : viewRegister.keySet()) {
            if (name.equals(viewConfig.getName())) {
                return viewConfig;
            }
        }
        return null;
    }

    @Override
    public void onActionEvent(String actionId) {
        showView(actionId);
    }

    private void showView(String name) {
        /*if (!applicationControl.isAuthenticated()) {
            sessionDialog.show(name);
            return;
        }*/
        applicationControl.setMessage("");
        ViewConfig viewConfig = getViewConfig(name);
        if (viewConfig == null) {
            throw new NullPointerException("View config not found " + name);
        }
        ViewHolder holder = viewRegister.get(viewConfig);
        if (holder == null) {
            throw new NullPointerException("View is not registered " + name);
        }
        Node node = nodeRegister.get(name);
        if (node == null) {
            holder.setApplicationControl(getApplicationControl());
            holder.setStage(getPrimaryStage());
            node = holder.createControl();
            nodeRegister.put(name, node);
        }
        if (currentView != null && !currentView.canCloseView()) {
            return;
        }
        String userName = applicationControl.getUserName() == null ? "" : applicationControl.getUserName();
        titleLabel.setText(viewConfig.getTitle() + " \t \t " + userName);
        contentPane.setCenter(node);
        currentView = holder;
        holder.openView(contentWidth, contentHeight);
    }

    public void closeAll() {
        viewRegister.values().forEach(ViewHolder::closeView);
    }

    private class MenuGroupPanel extends TitledPane implements EventHandler<ActionEvent> {
        private VBox itemPanel;

        MenuGroupPanel(String title) {
            setText(title);
            itemPanel = new VBox();
            itemPanel.setSpacing(6);
            itemPanel.setPadding(new Insets(6));
            setContent(itemPanel);
        }

        void addViewLink(ViewConfig config) {
            Hyperlink hyperlink = new Hyperlink(config.getTitle());
            hyperlink.setId(config.getName());
            hyperlink.setOnAction(this);
            itemPanel.getChildren().add(hyperlink);
        }

        @Override
        public void handle(ActionEvent event) {
            Hyperlink hyperlink = (Hyperlink) event.getSource();
            showView(hyperlink.getId());
        }
    }
}
