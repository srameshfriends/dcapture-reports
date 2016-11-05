package excel.accounting.dialog;

import excel.accounting.dao.SystemSettingDao;
import excel.accounting.entity.SystemSetting;
import excel.accounting.service.SystemSettingService;
import excel.accounting.shared.DataConverter;
import excel.accounting.ui.ActionHandler;
import excel.accounting.ui.StyleBuilder;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * Session Dialog
 *
 * @author Ramesh
 * @since Oct, 2016
 */
public class SessionDialog extends AbstractDialog {
    private PasswordField passwordField;
    private ToggleGroup toggleGroup;
    private Label messageLabel;
    private ActionHandler viewHandler;
    private String viewId = "";
    private SystemSettingDao systemSettingDao;

    public void setActionHandler(ActionHandler showHandler) {
        this.viewHandler = showHandler;
    }

    public void show(String viewId) {
        this.viewId = viewId;
        super.show();
    }

    @Override
    protected String getTitle() {
        return "Application Authority";
    }

    @Override
    protected void onOpenEvent() {
        passwordField.requestFocus();
        passwordField.selectAll();
    }

    @Override
    protected Parent create() {
        systemSettingDao = (SystemSettingDao) getBean("systemSettingDao");
        SystemSettingService systemSettingService = (SystemSettingService) getBean("systemSettingService");
        passwordField = new PasswordField();
        messageLabel = new Label();
        messageLabel.setLabelFor(passwordField);
        StyleBuilder builder = new StyleBuilder();
        builder.color("#ff0000");
        builder.fontSize(12);
        messageLabel.setStyle(builder.toString());
        passwordField.setOnKeyReleased(event -> {
            messageLabel.setText("");
            if (KeyCode.ENTER.equals(event.getCode())) {
                onSignInEvent();
            }
        });
        GridPane gridPane = new GridPane();
        gridPane.setVgap(12);
        gridPane.add(messageLabel, 0, 0);
        List<SystemSetting> systemUserList = systemSettingDao.getSystemUser();
        if(systemUserList.isEmpty()) {
            systemSettingService.insertDefaultSystemUser(false);
        }
        toggleGroup = new ToggleGroup();
        int index = 1;
        for (SystemSetting setting : systemUserList) {
            if(setting.getBoolValue() != null && !setting.getBoolValue()) {
                continue;
            }
            RadioButton radioButton = new RadioButton(setting.getTextValue());
            radioButton.setToggleGroup(toggleGroup);
            radioButton.setUserData(setting);
            gridPane.add(radioButton, 0, index);
            if(index == 1) {
                radioButton.setSelected(true);
            }
            index += 1;
        }
        toggleGroup.selectedToggleProperty().addListener((ov, oldToggle, newToggle) -> handleSwitchUserEvent());
        gridPane.add(passwordField, 0, 4);
        VBox basePanel = new VBox();
        basePanel.setPadding(new Insets(24));
        basePanel.getChildren().add(gridPane);
        //
        addAction("actionOkay", "Okay");
        addAction("actionCancel", "Cancel");
        return basePanel;
    }

    private void handleSwitchUserEvent() {
        messageLabel.setText("");
        passwordField.requestFocus();
        passwordField.selectAll();
    }

    private void onSignInEvent() {
        Toggle toggle = toggleGroup.getSelectedToggle();
        final SystemSetting selected = (SystemSetting) toggle.getUserData();
        String password = null;
        List<SystemSetting> userPass = systemSettingDao.findByGroupCode(selected.getCode());
        for (SystemSetting user : userPass) {
            if (!selected.getCode().equals(user.getCode())) {
                password = DataConverter.decode(user.getTextValue());
                break;
            }
        }
        String enteredPassword = passwordField.getText();
        if (enteredPassword != null && password != null && password.equals(enteredPassword)) {
            getApplicationControl().setAuthenticated(selected.getCode(), selected.getTextValue());
            hide();
            viewHandler.onActionEvent(viewId);
        } else {
            messageLabel.setText("Wrong Password");
        }
    }

    @Override
    protected void onActionEvent(final String actionId) {
        if ("actionOkay".equals(actionId)) {
            onSignInEvent();
        } else if ("actionCancel".equals(actionId)) {
            hide();
        }
    }
}
