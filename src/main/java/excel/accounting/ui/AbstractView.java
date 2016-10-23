package excel.accounting.ui;

import excel.accounting.db.DataProcessor;
import excel.accounting.shared.ApplicationControl;
import excel.accounting.ui.ViewHolder;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * Abstract View
 */
public abstract class AbstractView implements ViewHolder {
    private Stage primaryStage;
    private ApplicationControl applicationControl;
    private double panelWidth, panelHeight;
    private Map<String, Button> actionBtnMap;

    @Override
    public void setApplicationControl(ApplicationControl control) {
        this.applicationControl = control;
    }

    @Override
    public void setStage(final Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    private Map<String, Button> getActionBtnMap() {
        if (actionBtnMap == null) {
            actionBtnMap = new HashMap<>();
        }
        return actionBtnMap;
    }

    protected Button getButton(String actionId) {
        return getActionBtnMap().get(actionId);
    }

    protected Button createButton(String actionId, String title, EventHandler<ActionEvent> handler) {
        Button button = new Button(title);
        button.setId(actionId);
        button.setOnAction(handler);
        getActionBtnMap().put(actionId, button);
        return button;
    }

    protected void setActionDisable(boolean disable, String... actionArray) {
        getActionBtnMap();
        for (String actionId : actionArray) {
            Button button = actionBtnMap.get(actionId);
            if (button != null) {
                button.setDisable(disable);
            }
        }
    }

    @Override
    public Stage getPrimaryStage() {
        return primaryStage;
    }

    protected DataProcessor getDataProcessor() {
        return applicationControl.getDataProcessor();
    }

    protected Object getService(String name) {
        return applicationControl.getService(name);
    }

    protected void onResize(double width, double height) {
        if (panelWidth != width) {
            panelWidth = width;
            onWidthChanged(width);
        }
        if (panelHeight != height) {
            panelHeight = height;
            onHeightChanged(height);
        }
    }

    protected boolean confirmDialog(String title, String description) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText(title);
        alert.setContentText(description);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
}