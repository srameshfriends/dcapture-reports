package excel.accounting.dialog;

import excel.accounting.db.QueryBuilder;
import excel.accounting.db.Transaction;
import excel.accounting.shared.DataConverter;
import excel.accounting.ui.ActionHandler;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Session Dialog
 *
 * @author Ramesh
 * @since Oct, 2016
 */
public class SessionDialog extends AbstractDialog {
    private RadioButton[] userSelectionBox;
    private PasswordField passwordField;
    private Map<String, String[]> userMap;
    private ToggleGroup toggleGroup;
    private Label messageLabel;
    private ActionHandler viewHandler;
    private String viewId;

    public void setViewHandler(ActionHandler showHandler) {
        this.viewHandler = showHandler;
    }

    public void show(String viewId) {
        this.viewId = viewId;
        super.show();
    }

    @Override
    protected String getTitle() {
        return "Switch Application User";
    }

    @Override
    protected void onOpenEvent() {
        insertUserNotCreated();
        loadRecords();
        passwordField.requestFocus();
        passwordField.selectAll();
    }

    private void loadRecords() {
        QueryBuilder queryBuilder = getQueryBuilder("application-user", "loadAll");
        List<String[]> textArrayList = getDataReader().findStrings(queryBuilder);
        for (int index = 0; index < textArrayList.size(); index++) {
            String[] textArray = textArrayList.get(index);
            userMap.put(textArray[0], new String[]{textArray[1], textArray[2]});
            userSelectionBox[index].setId(textArray[0]);
            userSelectionBox[index].setText(textArray[1]);
        }
    }

    private void insertUserNotCreated() {
        QueryBuilder queryBuilder = getQueryBuilder("application-user", "totalRowCount");
        Long rowCount = (Long) getDataReader().findSingleObject(queryBuilder);
        if (rowCount != null && 2 < rowCount) {
            return;
        }
        queryBuilder = getQueryBuilder("application-user", "insert");
        Transaction transaction = createTransaction();
        transaction.setBatchQuery(queryBuilder);
        transaction.addBatch("U1", "User 1", DataConverter.encode("user1"));
        transaction.addBatch("U2", "User 2", DataConverter.encode("user2"));
        transaction.addBatch("U3", "User 3", DataConverter.encode("user3"));
        transaction.executeBatch();
    }

    @Override
    protected Parent create() {
        viewId = "";
        userMap = new HashMap<>();
        userSelectionBox = new RadioButton[3];
        passwordField = new PasswordField();
        messageLabel = new Label();
        messageLabel.setLabelFor(passwordField);
        passwordField.setOnKeyReleased(event -> {
            messageLabel.setText("");
            if (KeyCode.ENTER.equals(event.getCode())) {
                onSignInEvent();
            }
        });
        toggleGroup = new ToggleGroup();
        for (int index = 0; index < userSelectionBox.length; index++) {
            userSelectionBox[index] = new RadioButton("");
            userSelectionBox[index].setUserData("U" + (index + 1));
            userSelectionBox[index].setToggleGroup(toggleGroup);
        }
        toggleGroup.selectedToggleProperty().addListener((ov, oldToggle, newToggle) -> handleSwitchUserEvent());
        userSelectionBox[0].setSelected(true);
        //
        GridPane gridPane = new GridPane();
        gridPane.setVgap(24);
        gridPane.add(messageLabel, 0, 0);
        for (int index = 0; index < userSelectionBox.length; index++) {
            gridPane.add(userSelectionBox[index], 0, (index + 1));
        }
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
        final String userCode = (String) toggle.getUserData();
        String[] selected = userMap.get(userCode);
        String password = DataConverter.decode(selected[1]);
        String enteredPassword = passwordField.getText();
        if (enteredPassword != null && password != null && password.equals(enteredPassword)) {
            getApplicationControl().setAuthenticated(userCode, selected[0]);
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
