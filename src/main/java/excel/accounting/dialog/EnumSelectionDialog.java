package excel.accounting.dialog;

import javafx.scene.Parent;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Enum Selection Dialog
 *
 * @author Ramesh
 * @since Nov, 2016
 */
public class EnumSelectionDialog<T> extends AbstractDialog {
    private ToggleGroup toggleGroup;
    private List<T> valueList;

    @SafeVarargs
    public final void setValueList(T... enumArray) {
        valueList = new ArrayList<T>();
        Collections.addAll(valueList, enumArray);
    }

    @Override
    protected void onActionEvent(final String actionId) {
        if ("actionOkay".equals(actionId)) {
            hide();
        } else if ("actionCancel".equals(actionId)) {
            setCancelled(true);
            hide();
        }
    }

    @Override
    protected void onOpenEvent() {
    }

    @SuppressWarnings("unchecked")
    public T getSelected() {
       Toggle toggle = toggleGroup.getSelectedToggle();
        if(toggle == null) {
            return null;
        }
        return (T)toggle.getUserData();
    }

    @Override
    protected Parent create() {
        toggleGroup = new ToggleGroup();
        VBox basePanel = new VBox();
        basePanel.setSpacing(16);
        for(T value : valueList) {
            RadioButton btn = new RadioButton(value.toString());
            btn.setToggleGroup(toggleGroup);
            btn.setUserData(value);
            basePanel.getChildren().add(btn);
        }
        addAction("actionOkay", "Okay");
        addAction("actionCancel", "Cancel");
        return basePanel;
    }

    @Override
    protected String getTitle() {
        return "";
    }
}
