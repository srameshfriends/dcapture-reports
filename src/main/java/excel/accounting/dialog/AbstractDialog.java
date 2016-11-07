package excel.accounting.dialog;

import excel.accounting.db.*;
import excel.accounting.shared.ApplicationControl;
import excel.accounting.ui.StyleBuilder;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.sql.SQLException;

/**
 * Abstract Dialog
 *
 * @author Ramesh
 * @since Oct, 2016
 */
public abstract class AbstractDialog implements EventHandler<ActionEvent> {
    private ApplicationControl applicationControl;
    private DataReader dataReader;
    private Stage dialogStage;
    private boolean cancelled;
    private HBox actionBar;
    private VBox basePanel;

    public void initialize(ApplicationControl control, Stage primaryStage) {
        this.applicationControl = control;
        dataReader = new DataReader(applicationControl.getDataProcessor());
        dialogStage = new Stage();
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.initOwner(primaryStage);
        dialogStage.initStyle(StageStyle.UNDECORATED);
        actionBar = new HBox();
        actionBar.setSpacing(24);
        actionBar.setPadding(new Insets(18));
    }

    private void createBasePanel() {
        StyleBuilder titleStyle = new StyleBuilder();
        titleStyle.color("#0000ff");
        titleStyle.padding(8);
        titleStyle.fontSize(14);
        Label titleLabel = new Label(getTitle());
        titleLabel.setStyle(titleStyle.toString());
        basePanel = new VBox();
        StyleBuilder styleBuilder = new StyleBuilder();
        styleBuilder.padding(8);
        styleBuilder.border(8, 1, "solid");
        basePanel.setStyle(styleBuilder.toString());
        basePanel.getChildren().addAll(titleLabel, create(), actionBar);
        dialogStage.setScene(new Scene(basePanel));
    }

    protected void setMessage(String message) {
        applicationControl.setMessage(message);
    }

    protected DataReader getDataReader() {
        return dataReader;
    }

    protected Transaction createTransaction() {
        return new Transaction(applicationControl.getDataProcessor());
    }

    protected QueryBuilder getQueryBuilder(String sqlFileName, String queryName) {
        return applicationControl.getDataProcessor().getQueryBuilder(sqlFileName, queryName);
    }

    void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    ApplicationControl getApplicationControl() {
        return applicationControl;
    }

    protected Object getBean(String name) {
        return applicationControl.getBean(name);
    }

    void show() {
        if(basePanel == null) {
            createBasePanel();
        }
        dialogStage.show();
        onOpenEvent();
    }

    public void showAndWait() {
        if(basePanel == null) {
            createBasePanel();
        }
        onOpenEvent();
        dialogStage.showAndWait();
    }

    void hide() {
        dialogStage.hide();
    }

    protected abstract void onActionEvent(String actionId);

    @Override
    public final void handle(ActionEvent event) {
        Button button = (Button) event.getSource();
        onActionEvent(button.getId());
    }

    Button addAction(String actionId, String title) {
        Button button = new Button(title);
        button.setId(actionId);
        button.setOnAction(this);
        actionBar.getChildren().add(button);
        return button;
    }

    protected abstract Parent create();

    protected abstract String getTitle();

    protected void onOpenEvent() {
    }

    protected void onCloseEvent() {
    }

    protected void executeBatch(Transaction transaction) {
        try {
            transaction.executeBatch();
        } catch (SQLException ex) {
            setMessage(ex.getErrorCode() + " : " + ex.getMessage());
        }
    }
}
