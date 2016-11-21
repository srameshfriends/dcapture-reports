package excel.accounting.view;

import excel.accounting.dao.SystemSettingDao;
import excel.accounting.entity.SystemSetting;
import excel.accounting.service.SystemSettingService;
import excel.accounting.shared.DataConverter;
import excel.accounting.shared.StringRules;
import excel.accounting.ui.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.util.List;

/**
 * System Setting View
 *
 * @author Ramesh
 * @since Nov, 2016
 */
public class SystemSettingView extends AbstractView implements ViewHolder {
    private ReadableTableView<SystemSetting> tableView;
    private SystemSettingDao systemSettingDao;
    private SystemSettingService systemSettingService;
    private TabPane basePanel;

    @Override
    public ViewConfig getViewConfig() {
        return new ViewConfig(ViewGroup.Management, "systemSettingView", "System Settings");
    }

    @Override
    public Node createControl() {
        ViewListener viewListener = new ViewListener();
        systemSettingDao = (SystemSettingDao) getService("systemSettingDao");
        systemSettingService = (SystemSettingService) getService("systemSettingService");
        tableView = new ReadableTableView<SystemSetting>().create();
        tableView.addTextColumn("name", "Name").setPrefWidth(200);
        tableView.addTextColumn("textValue", "Value");
        tableView.addSelectionChangeListener(viewListener);
        tableView.setContextMenuHandler(viewListener);
        tableView.addContextMenuItem("modifyAction", "Modify");
        //
        basePanel = new TabPane();
        Tab userTab = new Tab();
        userTab.setText("System User");
        userTab.setContent(systemUserPane());
        basePanel.getTabs().add(userTab);
        return basePanel;
    }

    private Node systemUserPane() {
        final TextField fullName = new TextField();
        final PasswordField passwordField = new PasswordField();
        Button btn = new Button("Save");
        btn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                saveSystemUser(fullName.getText(), passwordField.getText());
            }
        });
        GridPane gridPane = new GridPane();
        gridPane.setVgap(24);
        gridPane.setPadding(new Insets(24));
        gridPane.add(new Label("Full Name"), 0, 0);
        gridPane.add(fullName, 1, 0);
        gridPane.add(new Label("Password"), 0, 1);
        gridPane.add(passwordField, 1, 1);
        gridPane.add(btn, 1, 2);
        return gridPane;
    }

    private void saveSystemUser(String fullName, String password) {
        if(StringRules.isEmpty(fullName)) {
            setMessage("Full name should not be empty");
            return;
        }
        if(StringRules.isEmpty(password)) {
            setMessage("Password should not be empty");
            return;
        }
        String code = getApplicationControl().getUserCode();
        List<SystemSetting> settingList = systemSettingDao.findByGroupCode(code);
        for(SystemSetting systemSetting : settingList) {
            if(code.equals(systemSetting.getCode())) {
                systemSetting.setTextValue(fullName);
            } else {
                systemSetting.setTextValue(DataConverter.encode(password));
            }
        }
        systemSettingService.updateValue(settingList);
        Platform.exit();
    }

    private HBox createToolbar() {
        Button refreshBtn;
        refreshBtn = createButton("refreshAction", "Refresh", event -> loadRecords());
        //
        HBox box = new HBox();
        box.setSpacing(12);
        box.setPadding(new Insets(12));
        box.getChildren().add(refreshBtn);
        return box;
    }

    @Override
    public boolean canCloseView() {
        return true;
    }

    @Override
    public void closeView() {
    }

    @Override
    public void openView(double width, double height) {
        onResize(width, height);
        onRowSelectionChanged(false);
        loadRecords();
    }

    @Override
    public void onWidthChanged(double width) {
        basePanel.setPrefWidth(width);
    }

    @Override
    public void onHeightChanged(double height) {
        basePanel.setPrefHeight(height);
    }

    private void loadRecords() {
        List<SystemSetting> settings = systemSettingDao.loadAll();
        if (settings == null || settings.isEmpty()) {
            return;
        }
        ObservableList<SystemSetting> observableList = FXCollections.observableArrayList(settings);
        tableView.setItems(observableList);
    }

    private void onRowSelectionChanged(boolean isRowSelected) {
        tableView.setDisable(!isRowSelected);
    }

    private void performActionEvent(final String actionId) {
        setMessage("");
    }

    private class ViewListener implements ListChangeListener<Integer>, ActionHandler {
        @Override
        public void onChanged(Change<? extends Integer> change) {
            onRowSelectionChanged(0 < change.getList().size());
        }

        @Override
        public void onActionEvent(final String actionId) {
            performActionEvent(actionId);
        }
    }
}
