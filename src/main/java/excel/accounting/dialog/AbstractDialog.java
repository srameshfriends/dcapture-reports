package excel.accounting.dialog;

import excel.accounting.db.*;
import excel.accounting.shared.AbstractControl;
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
public abstract class AbstractDialog extends AbstractControl implements EventHandler<ActionEvent> {
    private Stage dialogStage;
    private boolean cancelled;
    private HBox actionBar;
    private VBox basePanel;

    public void start(Stage primaryStage) {
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

    protected Transaction createTransaction() {
        return new Transaction(getApplicationControl().getConnectionPool());
    }

    protected QueryBuilder getQueryBuilder(String sqlFileName, String queryName) {
        return getDataProcessor().getQueryBuilder(sqlFileName, queryName);
    }

    protected void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public boolean isCancelled() {
        return cancelled;
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

    public void hide() {
        dialogStage.hide();
    }

    protected abstract void onActionEvent(String actionId);

    @Override
    public final void handle(ActionEvent event) {
        Button button = (Button) event.getSource();
        onActionEvent(button.getId());
    }

    public Button addAction(String actionId, String title) {
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
