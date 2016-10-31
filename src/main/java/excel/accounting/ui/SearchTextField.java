package excel.accounting.ui;

import com.sun.org.apache.xpath.internal.operations.Bool;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Search Text Field
 *
 * @author Ramesh
 * @since Oct, 2016
 */
public class SearchTextField extends TextField {
    private ActionHandler actionHandler;

    public SearchTextField() {
        setOnKeyReleased(event -> {
            if (KeyCode.ENTER.equals(event.getCode())) {
                actionHandler.onActionEvent("actionSearch");
            }
        });
    }

    public void setActionHandler(ActionHandler actionHandler) {
        this.actionHandler = actionHandler;
    }
}
