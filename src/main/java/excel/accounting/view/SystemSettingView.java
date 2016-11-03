package excel.accounting.view;

import excel.accounting.dao.SystemSettingDao;
import excel.accounting.entity.SystemSetting;
import excel.accounting.service.SystemSettingService;
import excel.accounting.ui.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

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
    private VBox basePanel;

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
        basePanel = new VBox();
        basePanel.getChildren().addAll(createToolbar(), tableView.getTableView());
        return basePanel;
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
        System.out.println(actionId);
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
