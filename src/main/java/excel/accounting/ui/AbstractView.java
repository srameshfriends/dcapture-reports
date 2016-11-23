package excel.accounting.ui;

import excel.accounting.shared.ApplicationControl;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Abstract View
 */
public abstract class AbstractView implements ViewHolder {
    private Stage primaryStage;
    private ApplicationControl applicationControl;
    private double panelWidth, panelHeight;
    private Map<String, Button> actionBtnMap;
    private Map<Integer, Button> actionMap;

    @Override
    public void setApplicationControl(ApplicationControl control) {
        this.applicationControl = control;
    }

    @Override
    public void setStage(final Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    protected void setMessage(String message) {
        applicationControl.setMessage(message);
    }

    private Map<String, Button> getActionBtnMap() {
        if (actionBtnMap == null) {
            actionBtnMap = new HashMap<>();
        }
        return actionBtnMap;
    }

    private Map<Integer, Button> getActionMap() {
        if (actionMap == null) {
            actionMap = new HashMap<>();
        }
        return actionMap;
    }

    protected Button getAction(int actionId) {
        return getActionMap().get(actionId);
    }

    @Deprecated
    protected Button createButton(String actionId, String title, EventHandler<ActionEvent> handler) {
        Button button = new Button(title);
        button.setId(actionId);
        button.setOnAction(handler);
        getActionBtnMap().put(actionId, button);
        return button;
    }

    protected Button createButton(int pid, String title, EventHandler<ActionEvent> handler) {
        Button button = new Button(title);
        button.setOnAction(handler);
        button.setUserData(pid);
        getActionMap().put(pid, button);
        return button;
    }

    protected void disableAction(boolean disable, int... actionArray) {
        for (int actionId : actionArray) {
            Button button = getActionMap().get(actionId);
            if (button != null) {
                button.setDisable(disable);
            }
        }
    }

    @Override
    public Stage getPrimaryStage() {
        return primaryStage;
    }

    protected ApplicationControl getApplicationControl() {
        return applicationControl;
    }

    protected Object getService(String name) {
        return applicationControl.getBean(name);
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

    protected boolean confirmDialog(String description) {
        return confirmDialog(null, description);
    }

    protected boolean confirmDialog(String title, String description) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText(title);
        alert.setContentText(description);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    protected void showErrorMessage(String description) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(null);
        alert.setContentText(description);
        alert.showAndWait();
    }
}
