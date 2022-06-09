function DataTableOrderBy(col, ele) {
    let self = this;
    self.column = col;
    self.node = ele;
    self.orderBy = "";
    self.setOrderBy = function (text) {
        self.orderBy = text;
        if ("ASC" === text) {
            self.node.innerHTML = "&#8659;";
        } else if ("DESC" === text) {
            self.node.innerHTML = "&#8657;";
        } else {
            self.node.innerHTML = "";
        }
    }
}

DataTableOrderBy.contains = function (array, name) {
    for (let xi = 0; xi < array.length; xi++) {
        let dtoBy = array[xi];
        if (name === dtoBy.column) {
            return true;
        }
    }
    return false;
};

DataTableOrderBy.get = function (array, name) {
    for (let xi = 0; xi < array.length; xi++) {
        let dtoBy = array[xi];
        if (name === dtoBy.column) {
            return dtoBy;
        }
    }
    return false;
};

DataTableOrderBy.remove = function (array, name) {
    for (let xi = 0; xi < array.length; xi++) {
        if (array[xi].column === name) {
            let item = array.splice(xi, 1);
            return item[0];
        }
    }
};

DataTableOrderBy.getOrderBy = function (array) {
    let result = [];
    for (let xi = 0; xi < array.length; xi++) {
        let dtoBy = array[xi];
        if (0 < xi) {
            result.push(", ");
        }
        result.push(dtoBy.column);
        result.push(" ");
        result.push(dtoBy.orderBy);
    }
    return 0 < result.length ? result.join("") : "";
};

function DataTableRow(rowId, columnCount, data, status) {
    this.readOnly = false;
    this.selected = false;
    this.rowId = rowId;
    this.data = data;
    this.status = [];
    this.values = [];
    this.fields = [];
    for (let idx = 0; idx < columnCount; idx++) {
        this.status[idx] = status;
    }
    return this;
}

DataTableRow.prototype.focus = function(colIndex) {
    if(typeof colIndex === "number" && colIndex < this.fields.length) {
        this.fields[colIndex].focus();
    }
};

DataTableRow.prototype.getStatus = function () {
    let hasError = false, hasModified = false;
    for (let idx = 0; idx < this.status.length; idx++) {
        if ("modified" === this.status[idx]) {
            hasModified = true;
        }
        if ("error" === this.status[idx]) {
            hasError = true;
        }
    }
    if (true === hasError) {
        return "error";
    }
    if (true === hasModified) {
        return "modified";
    }
    return "info";
};

DataTableRow.prototype.clearStatus = function () {
    for (let idx = 0; idx < this.status.length; idx++) {
        this.status[idx] = 'info';
    }
};

DataTableRow.prototype.toString = function () {
    return this.rowId;
};

function DataTableUtil() {

}

DataTableUtil.toCurrencyObject = function (model, data) {
    let result = false;
    if(typeof data === "object" && typeof model.reference === "string") {
        result = data[model.reference];
    }
    if(!result) {
        if(typeof model.currency === "function") {
            result = model.currency();
        } else if(typeof model.currency === "object") {
            result = model.currency;
        } else {
            result = data['currency'];
        }
    }
    if(typeof result === "object") {
        return result;
    } else if(typeof result === "string") {
        const curMap = DCapture.getValue("currency-map");
        if(typeof curMap === "object") {
            return curMap[result];
        }
    }
}

DataTableUtil.parseCurrency = function (text, cfg) {
    if (text === null || (typeof text === "undefined") || 0 === text.length) {
        return 0.0;
    }
    let precision = 2, symbol = "";
    if(typeof cfg === "object" && cfg !== null) {
        if(typeof cfg.precision === "number") {
            precision = cfg.precision;
            if (8 < cfg.precision) {
                precision = 8;
            }
        }
        if(typeof cfg.symbol === "string") {
            symbol = cfg.symbol;
            if (3 < symbol.length) {
                symbol = symbol.substring(0, 2);
            }
        }
    }
    let isNegative = false;
    if (typeof text === "string") {
        text = text.replace(symbol, "");
        if (text.startsWith("-") || (text.startsWith("(") && text.endsWith(")"))) {
            isNegative = true;
        }
        text = text.trim().replace(/[^0-9.]/g, "");
    } else {
        text = "";
    }
    if ("" === text.trim()) {
        return 0.0;
    }
    if (isNegative) {
        text = "-" + text;
    }
    const value = parseFloat(text);
    if (isNaN(value) || value === 0) {
        return 0.0;
    }
    return parseFloat(value.toFixed(precision));
};

DataTableUtil.formatCurrency = function (decimal, cfg) {
    if (typeof decimal !== "number" || Number.isNaN(decimal) || decimal === 0) {
        return "";
    }
    let precision = 4, code = "", symbol = "", isMinusPrefix = window.dcapture.isMinusPrefix;
    if (cfg !== null && typeof cfg === "object") {
        if (typeof cfg.fk === "string" && 3 === cfg.fk.length) {
            code = cfg.fk.toUpperCase();
        }
        if (typeof cfg.symbol === "string" && 4 > cfg.symbol.length) {
            symbol = cfg.symbol;
        }
        if (typeof cfg.precision === "number") {
            if (-1 < cfg.precision && 9 > cfg.precision) {
                precision = cfg.precision;
            }
        }
    }
    decimal = decimal.toFixed(precision);
    if ("INR" === code) {
        return symbol + DCapture.pvtFormatPattern(decimal, isMinusPrefix, true);
    }
    return symbol + DCapture.pvtFormatPattern(decimal, isMinusPrefix, false);
};

DataTableUtil.getText = function (model, data, value) {
    if (typeof model.getText === "function") {
        return model.getText(model, data, value);
    }
    if (typeof value === "undefined" || value === null) {
        return "";
    } else if ("string" === model.type) {
        if (typeof value === "string") {
            return value;
        }
        return value.toString();
    } else if ("int" === model.type || "decimal" === model.type) {
        if (isNaN(value) || 0 === value) {
            return "";
        }
    } else if ("currency" === model.type) {
        return DataTableUtil.formatCurrency(value, DataTableUtil.toCurrencyObject(model, data));
    } else if ("date" === model.type) {
        return DCapture.getDateText(value);
    } else if ("boolean" === model.type || "checkbox" === model.type) {
        return value;
    } else if ("range" === model.type) {
        if (typeof value === "number") {
            return value;
        }
        let range = parseInt(value);
        if (isNaN(range)) {
            return 0;
        }
        return range;
    } else if ("percentage" === model.type) {
        if(typeof value === "number") {
            if(isNaN(value)) {
                return '';
            }
            return 0 === value ? '' : value + '%';
        } else if(typeof value === "string") {
            if(0 < value.indexOf('%')) {
                return value;
            }
            return value + '%';
        }
        return '%';
    } else if ("text" === model.type) {
        if (typeof value === "string") {
            return value.replace("\r\n", "\\r\\n");
        }
        return value.toString();
    }
    return value.toString();
};

DataTableUtil.removeInvalidRow = function (dataTable, colName, colType) {
    const rows = dataTable.getSelectedTableRow(), array = [];
    if(0 === rows.length) {
        return;
    }
    for (let idx = 0; idx < rows.length; idx++) {
        let tableRow = rows[idx], data = tableRow.data, value = data[colName];
        if (typeof value === "undefined") {
            dataTable.removeRowById(tableRow.rowId);
        } else if("number" === colType && 0 === value) {
            dataTable.removeRowById(tableRow.rowId);
        } else if("string" === colType && 0 === value.length) {
            dataTable.removeRowById(tableRow.rowId);
        }
    }
};

function DataTable() {
    let self = this;
    self.manageOrderBy = function (cell, model) {
        cell.addEventListener("click", function (evt) {
            evt.preventDefault();
            let orderByObj, modelName = model.name;
            if (DataTableOrderBy.contains(self.dataTableOrderBy, modelName)) {
                orderByObj = DataTableOrderBy.remove(self.dataTableOrderBy, modelName);
            } else {
                orderByObj = new DataTableOrderBy(modelName, cell.childNodes[1]);
            }
            self.dataTableOrderBy.push(orderByObj);
            if ("" === orderByObj.orderBy) {
                orderByObj.setOrderBy("ASC");
            } else if ("ASC" === orderByObj.orderBy) {
                orderByObj.setOrderBy("DESC");
            } else {
                orderByObj.setOrderBy("");
                DataTableOrderBy.remove(self.dataTableOrderBy, orderByObj.column);
            }
            if (3 === self.dataTableOrderBy.length) {
                self.dataTableOrderBy.pop().setOrderBy("");
            }
            if (typeof self.onOrderByEvent === "function") {
                self.onOrderByEvent(DataTableOrderBy.getOrderBy(self.dataTableOrderBy));
            }
        });
    };
    self.getOrderBy = function () {
        return DataTableOrderBy.getOrderBy(self.dataTableOrderBy);
    };
    self.pvtNextRowId = function () {
        self.rowSequence = self.rowSequence + 1;
        return "" + self.rowSequence;
    };
    self.pvtAddFirstColumn = function () {
        let cell = document.createElement("TH"), selectable = document.createElement("DIV");
        cell.setAttribute("class", 'data-table-th');
        cell.style.width = '44px';
        self.header.appendChild(cell);
        selectable.innerHTML = "<div class='data-table-row-menu'><span class='data-table-row-icon'></span></div>";
        selectable.setAttribute("class", 'd-inline');
        cell.setAttribute("class", "data-table-td pointer");
        cell.setAttribute("data-selected", "false");
        cell.appendChild(selectable);
        if (self.isSingleSelection) {
            return;
        }
        selectable.addEventListener("click", function (evt) {
            evt.preventDefault();
            let text = cell.getAttribute("data-selected");
            const status = "true" !== text;
            cell.setAttribute("data-selected", (status ? "true" : "false"));
            for (const pair of self.tableRows) {
                pair[1].selected = status;
                if (status) {
                    DCapture.addClass(pair[1].row, "row-selected");
                    pair[1].selected = true;
                } else {
                    DCapture.removeClass(pair[1].row, "row-selected");
                    pair[1].selected = false;
                }
            }
            let tableRow = 0 === self.tableRows.size ? false : Array.from(self.tableRows.keys())[0];
            if (typeof self.onSelected === "function" && tableRow) {
                self.onSelected(status, tableRow);
            }
        });
    };
    self.pvtAddColumn = function (model) {
        if (typeof model.name === "undefined") {
            throw "DataTable column name should not be empty ( " + JSON.stringify(model) + " )";
        }
        if (typeof model.title === "undefined") {
            model.title = model.name;
        }
        if (typeof model.type === "undefined") {
            model.type = "string";
        }
        if ("currency" === model.type && typeof model.currency === "undefined") {
            model.currency = "currency";
        } else if ("icon" === model.type && typeof model.html === "undefined") {
            model.html = "<i></i>";
        }
        if (self.readOnly) {
            model.readOnly = true;
        }
        model.isDisableBlurEvent = ("date" === model.type || "boolean" === model.type || "checkbox" === model.type
            || "button" === model.type || "icon" === model.type || "time" === model.type);
        let field, cell = document.createElement("TH");
        self.header.appendChild(cell);
        cell.setAttribute("class", 'data-table-th');
        cell.setAttribute("data-bs-toggle", 'tooltip');
        cell.setAttribute("title", model.title);
        cell.innerHTML = "<div class='data-table-col-title'></div>" +
            "<div class='data-table-col-icon'></div>";
        field = cell.childNodes[0];
        field.innerText = model.title;
        if (typeof model.width === "string") {
            cell.style.width = model.width;
        }
        if (typeof model.minWidth === "string") {
            cell.style.minWidth = model.minWidth;
        }
        if ("int" === model.type || "decimal" === model.type || "currency" === model.type || "percentage" === model.type) {
            cell.style.textAlign = "right";
        }
        if (model.orderBy) {
            self.manageOrderBy(cell, model);
        }
    };
    self.findRowById = function (rowId) {
        let nodeArray = self.body.childNodes;
        for (let idx = 0; idx < nodeArray.length; idx++) {
            let itemId, row = nodeArray[idx];
            itemId = row.getAttribute("data-id");
            if (rowId === itemId) {
                return row;
            }
        }
        return false;
    };
    self.build = function (args) {
        if (typeof args === "undefined") {
            throw "DataTable configuration should not be null";
        }
        if (typeof args.parent === "undefined") {
            throw "DataTable parent element or id should not be null";
        }
        self.limit = 20;
        self.isSourceFromCsv = false;
        self.readOnly = typeof args.readOnly === "boolean" ? args.readOnly : false;
        self.columns = args.columns;
        self.renderer = args.renderer;
        self.onOrderByEvent = args.onOrderByEvent;
        self.onActionEvent = args.onActionEvent;
        self.getRowCommands = args.getRowCommands;
        self.onValueChanged = args.onValueChanged;
        self.onKeyEvent = args.onKeyEvent;
        self.isRowEditable = args.isRowEditable;
        self.tableRows = new Map();
        self.onSelected = args.onSelected;
        self.isSingleSelection = args.isSingleSelection;
        if (typeof args.limit === "number") {
            if (1 > args.limit) {
                self.limit = 20;
            } else if (100 < args.limit) {
                self.limit = 100;
            }
        } else if (typeof window.dcapture.paginationLimit === "number") {
            self.limit = window.dcapture.paginationLimit;
        }
        if (typeof args.parent === "string") {
            self.parent = document.getElementById(args.parent);
        } else {
            self.parent = args.parent;
        }
        self.rowSequence = 1;
        self.table = document.createElement("TABLE");
        self.table.setAttribute("class", "data-table");
        self.parent.appendChild(self.table);
        let tHead = document.createElement("THEAD");
        tHead.setAttribute("class", "data-table-thead");
        self.body = document.createElement("TBODY");
        self.table.appendChild(tHead);
        self.table.appendChild(self.body);
        self.dataTableOrderBy = [];
        self.header = document.createElement("TR");
        tHead.appendChild(self.header);
        self.pvtAddFirstColumn();
        for (let cdx = 0; cdx < self.columns.length; cdx++) {
            self.pvtAddColumn(self.columns[cdx]);
        }
        return this;
    };
    self.setWidth = function (width) {
        if (typeof width !== "string") {
            width = "auto";
        }
        self.table.style.width = width;
    };
    self.getRowById = function (rowId) {
        let nodeArray = self.body.childNodes;
        for (let idx = 0; idx < nodeArray.length; idx++) {
            let name, row = nodeArray[idx];
            name = row.getAttribute("data-id");
            if (name === rowId) {
                return row;
            }
        }
        return false;
    };
    self.removeRowById = function (rowId) {
        let nodeArray = self.body.childNodes;
        for (let idx = 0; idx < nodeArray.length; idx++) {
            let name, row = nodeArray[idx];
            name = row.getAttribute("data-id");
            if (rowId === name) {
                self.body.removeChild(row);
                self.tableRows.delete(rowId);
            }
        }
        return false;
    };
    self.getSiblingRowId = function (rowId) {
        let nodeArray = self.body.childNodes;
        const rowCount = nodeArray.length;
        for (let idx = 0; idx < rowCount; idx++) {
            let name, row = nodeArray[idx];
            name = row.getAttribute("data-id");
            if (name === rowId) {
                if (rowCount < (idx + 1)) {
                    row = nodeArray[idx + 1];
                    return row.getAttribute("data-id");
                }
                break;
            }
        }
        return false;
    };
    self.getTableRows = function () {
        let array = [];
        self.tableRows.forEach(function (row) {
            array.push(row);
        });
        return array;
    };
    self.getSelectedTableRow = function (validOnly) {
        let array = [];
        if (typeof validOnly === "boolean" && validOnly) {
            self.tableRows.forEach(function (item) {
                if (true === item.selected && self.isValid(item.data)) {
                    array.push(item);
                }
            });
        } else {
            self.tableRows.forEach(function (item) {
                if (true === item.selected) {
                    array.push(item);
                }
            });
        }
        return array;
    };
    self.setSelected = function (tableRow, isSelected, silently) {
        if (tableRow.selected && !isSelected) {
            DCapture.removeClass(tableRow.row, "row-selected");
            tableRow.selected = false;
        } else if (!tableRow.selected && isSelected) {
            DCapture.addClass(tableRow.row, "row-selected");
            tableRow.selected = true;
        }
        if (typeof self.onSelected === "function" && !silently) {
            self.onSelected(tableRow.selected, tableRow);
        }
    };
    self.getSelected = function (validOnly) {
        let array = [];
        if (typeof validOnly === "boolean" && validOnly) {
            self.tableRows.forEach(function (item) {
                if (true === item.selected && self.isValid(item.data)) {
                    array.push(item.data);
                }
            });
        } else {
            self.tableRows.forEach(function (item) {
                if (true === item.selected) {
                    array.push(item.data);
                }
            });
        }
        return array;
    };
    self.isSelected = function () {
        const keyArray = Array.from(self.tableRows.keys());
        for (let idx = 0; idx < keyArray.length; idx++) {
            let key = keyArray[idx], tblRow = self.tableRows.get(key);
            if (true === tblRow.selected) {
                return true;
            }
        }
        return false;
    };
    self.removeSelected = function () {
        let resultArray = [];
        const keyArray = Array.from(self.tableRows.keys());
        for (let idx = 0; idx < keyArray.length; idx++) {
            let key = keyArray[idx], tblRow = self.tableRows.get(key);
            if (true === tblRow.selected) {
                resultArray.push(tblRow.data);
                self.removeRowById(tblRow.rowId);
            }
        }
        return resultArray;
    };
    self.setRowStatus = function (row, status) {
        let circleNode, firstCell = row.childNodes[0];
        circleNode = firstCell.firstChild.firstChild.firstChild;
        circleNode.setAttribute("data-input", status);
    };
    self.pvtReadOnlyRow = function (tableRow, status) {
        tableRow.readOnly = status;
        let array = tableRow.fields;
        for (let idx = 0; idx < array.length; idx++) {
            let field = array[idx];
            field.readOnly = status;
        }
    };
    self.setReadOnlyRow = function (tableRow, status) {
        status = status === true;
        self.pvtReadOnlyRow(tableRow, status);
    };
    self.setReadOnly = function (status) {
        status = status === true;
        for (const pair of self.tableRows) {
            self.pvtReadOnlyRow(pair[1], status);
        }
    };
    self.setModified = function (idOrTblRowOrRow, colIndex) {
        if (typeof idOrTblRowOrRow === "string") {
            if (self.tableRows.has(idOrTblRowOrRow)) {
                let tableRow = self.getRowById(idOrTblRowOrRow);
                self.setRowStatus(tableRow.row, "modified");
                if (typeof colIndex === "number") {
                    tableRow.status[colIndex] = "modified";
                }
            }
        } else if (idOrTblRowOrRow instanceof DataTableRow) {
            if (typeof colIndex === "number") {
                idOrTblRowOrRow.status[colIndex] = "modified";
            }
            self.setRowStatus(idOrTblRowOrRow.row, "modified");
        }
    };
    self.clearModified = function (idOrTblRowOrRow) {
        if (typeof idOrTblRowOrRow === "string") {
            if (self.tableRows.has(idOrTblRowOrRow)) {
                let tableRow = self.getRowById(idOrTblRowOrRow);
                tableRow.clearStatus();
                self.setRowStatus(tableRow.row, 'info');
            }
        } else if (idOrTblRowOrRow instanceof DataTableRow) {
            idOrTblRowOrRow.clearStatus();
            self.setRowStatus(idOrTblRowOrRow.row, 'info');
        }
    };
    self.getModifiedTableRow = function () {
        let array = [];
        self.tableRows.forEach(function (tableRow) {
            if ("modified" === tableRow.getStatus()) {
                array.push(tableRow);
            }
        });
        return array;
    };
    self.getModified = function () {
        let array = [];
        self.tableRows.forEach(function (tableRow) {
            if ("modified" === tableRow.getStatus()) {
                array.push(tableRow.data);
            }
        });
        return array;
    };
    self.isModified = function (tableRow) {
        if(tableRow instanceof DataTableRow) {
            if ("modified" === tableRow.getStatus()) {
                return true;
            }
        } else {
            for (const pair of self.tableRows) {
                let row = pair[1];
                if ("modified" === row.getStatus()) {
                    return true;
                }
            }
        }
        return false;
    }
    self.getData = function () {
        let array = [];
        self.tableRows.forEach(function (tableRow) {
            array.push(tableRow.data);
        });
        return array;
    };
    self.clearAll = function () {
        while (self.body.firstChild) {
            self.body.firstChild.remove();
        }
        if (self.tableRows instanceof Map) {
            self.tableRows.clear();
        }
        self.tableRows = new Map();
        self.rowSequence = 1;
        return true;
    };
    self.getColumnCount = function () {
        return self.columns.length;
    };
    self.getRowCount = function () {
        return DCapture.getInt(self.tableRows.size);
    };
    self.isData = function () {
        return self.tableRows.size > 0;
    };
    self.showActionDialog = function (rowId, commands, evt) {
        let idx, content = document.createElement("div");
        content.setAttribute("class", "list-group");
        for (idx = 0; idx < commands.length; idx++) {
            let model = commands[idx];
            if (typeof model === "undefined" || model.id === undefined) {
                continue;
            }
            const actionId = model.id;
            let btn = document.createElement("input");
            btn.setAttribute("type", "button");
            btn.setAttribute("class", "list-group-item list-group-item-action pointer text-primary");
            btn.value = model.title === undefined ? model.id : model.title;
            btn.addEventListener("click", function (evt) {
                evt.preventDefault();
                CustomDialog.hide(true);
                self.onActionEvent(actionId, self.tableRows.get(rowId), -1, evt);
            });
            content.appendChild(btn);
        }
        CustomDialog.show(content, evt, function () {
            CustomDialog.hide(true);
        });
    };
    self.isValid = function (data) {
        for (let idx = 0; idx < self.columns.length; idx++) {
            const model = self.columns[idx];
            let value = DCapture.getDeepValue(data, model.name);
            value = DCapture.typeSafe(model, data, value);
            if (model.required && DCapture.isEmpty(model.type, value)) {
                return false;
            }
        }
        return true;
    };
    self.getColumnIndex = function (colName) {
        if(typeof colName === "string") {
            for (let idx = 0; idx < self.columns.length; idx++) {
                const model = self.columns[idx];
                if(colName === model.name) {
                    return idx;
                }
            }
        }
        return -1;
    };
    self.getModel = function (colName) {
        if(typeof colName === "string") {
            for (let idx = 0; idx < self.columns.length; idx++) {
                const model = self.columns[idx];
                if(colName === model.name) {
                    return model;
                }
            }
        }
    };
    self.setRowReferenceValue = function (row, referenceColumn, columnName, obj) {
        const colIndex = self.getColumnIndex(columnName);
        const ref = row.data[referenceColumn];
        const refValue = obj[ref];
        row.data[columnName] = refValue;
        if(-1 !== colIndex) {
            self.pvtOnValueChanged(row, colIndex, row.fields[colIndex], refValue, true, true);
        }
    };
    self.setReferenceValue = function (referenceColumn, columnName, obj) {
        const colIndex = self.getColumnIndex(columnName);
        self.tableRows.forEach(function (row) {
            const ref = row.data[referenceColumn];
            const refValue = obj[ref];
            row.data[columnName] = refValue;
            if(-1 !== colIndex) {
              self.pvtOnValueChanged(row, colIndex, row.fields[colIndex], refValue, true, true);
            }
        });
    };
    self.pvtOnValueChanged = function (rowId, colIndex, field, newValue, silently, unmodified) {
        const model = self.columns[colIndex];
        let tableRow = rowId;
        if(!(rowId instanceof DataTableRow)) {
            tableRow = self.tableRows.get(rowId);
        }
        if (newValue instanceof Date) {
            let mon = newValue.getMonth() + 1, day = newValue.getDate();
            if (10 > mon) {
                mon = "0" + mon;
            }
            if (10 > day) {
                day = "0" + day;
            }
            let dateText = newValue.getFullYear() + "-" + mon + "-" + day;
            DCapture.setDeepValue(tableRow.data, model, dateText);
        } else {
            DCapture.setDeepValue(tableRow.data, model, newValue);
        }
        if ("boolean" === model.type || "checkbox" === model.type) {
            field.checked = newValue;
        } else {
            const tagName = field.tagName.toUpperCase();
            if ("INPUT" === tagName) {
                field.value = DataTableUtil.getText(model, tableRow.data, newValue);
            } else if ("icon" === model.type) {
                field.innerHTML = model.html;
            } else {
                field.innerText = DataTableUtil.getText(model, tableRow.data, newValue);
            }
        }
        let oldVal, status = "info";
        field = tableRow.fields[colIndex];
        oldVal = tableRow.values[colIndex];
        const hasEmpty = DCapture.isEmpty(model.type, newValue);
        const hasModified = DCapture.isModified(model.type, oldVal, newValue);
        if (model.required && hasEmpty) {
            status = "error";
        } else if (hasModified) {
            status = true === unmodified ? "info" : "modified";
        }
        tableRow.status[colIndex] = status;
        field.setAttribute("data-input", status);
        self.setRowStatus(tableRow.row, tableRow.getStatus());
        if (typeof silently !== "boolean" || !silently) {
            if ("modified" === status && (typeof self.onValueChanged === "function")) {
                self.onValueChanged(model.name, tableRow, colIndex, newValue, oldVal);
            }
        }
    };
    self.setValue = function (rowIdOrTableRow, columnIndexOrName, value, silently) {
        let colIndex = columnIndexOrName;
        if (typeof columnIndexOrName == "string") {
            colIndex = self.getColumnIndex(columnIndexOrName);
        }
        if(-1 === colIndex) {
            throw "DataTable set value column index should not be negative."
        }
        if (rowIdOrTableRow instanceof DataTableRow) {
            self.pvtOnValueChanged(rowIdOrTableRow.rowId, colIndex, rowIdOrTableRow.fields[colIndex], value, silently);
        } else {
            let tableRow = self.tableRows.get(rowIdOrTableRow);
            if (tableRow) {
                self.pvtOnValueChanged(rowIdOrTableRow, colIndex, tableRow.fields[colIndex], value, silently);
            }
        }
    };
    self.getValue = function (rowIdOrTableRow, name) {
        let tableRow;
        if (rowIdOrTableRow instanceof DataTableRow) {
            tableRow = rowIdOrTableRow;
        } else {
            tableRow = self.tableRows.get(rowIdOrTableRow);
        }
        return tableRow.data[name];
    };
    self.getFieldValue = function (rowIdOrTableRow, name) {
        let tableRow, colIndex = self.getColumnIndex(name);
        if (rowIdOrTableRow instanceof DataTableRow) {
            tableRow = rowIdOrTableRow;
        } else {
            tableRow = self.tableRows.get(rowIdOrTableRow);
        }
        let field = tableRow.fields[colIndex], model = self.getModel(name);
        if ("boolean" === model.type || "checkbox" === model.type) {
            return field.checked;
        } else {
            const tagName = field.tagName.toUpperCase();
            if ("INPUT" === tagName) {
                 return DCapture.typeSafe(model, tableRow.data, field.value.trim());
            } else {
                 return DCapture.typeSafe(model, tableRow.data, field.innerText);
            }
        }
    };
    self.pvtAddFirstCell = function (row, rowId) {
        let selectable, actionEle, cell = document.createElement("TD");
        selectable = document.createElement("DIV");
        actionEle = document.createElement("DIV");
        actionEle.style.float = "right";
        selectable.innerHTML = "<div class='data-table-row-menu'><span class='data-table-row-icon'></span></div>";
        actionEle.innerHTML = "<div class='data-table-row-menu'><span>:</span></div>";
        actionEle.setAttribute("class", "d-inline pointer pr-2");
        cell.setAttribute("class", "data-table-td pointer");
        selectable.setAttribute("class", 'd-inline');
        cell.appendChild(selectable);
        cell.appendChild(actionEle);
        selectable.addEventListener("click", function (evt) {
            evt.preventDefault();
            let tblRow = self.tableRows.get(rowId), isSelected = tblRow.selected;
            if(self.isSingleSelection) {
                self.removeSelected();
                if(isSelected) {
                   return;
                }
            }
            self.setSelected(tblRow, !tblRow.selected, false);
        });
        if (typeof self.onActionEvent === "function") {
            actionEle.addEventListener("click", function (evt) {
                evt.preventDefault();
                if (typeof self.getRowCommands === "function") {
                    let tableRow2 = self.tableRows.get(rowId);
                    let actions = self.getRowCommands(tableRow2);
                    if (Array.isArray(actions)) {
                        self.showActionDialog(rowId, actions, evt);
                    }
                } else {
                    self.onActionEvent("click", self.tableRows.get(rowId), -1, evt);
                }
            });
        }
        row.append(cell);
        return cell;
    };
    self.appendCell = function (row, tableRow, colIndex) {
        const model = self.columns[colIndex];
        let readOnlyTemp = typeof self.readOnly === "boolean" && self.readOnly;
        if(!readOnlyTemp) {
            readOnlyTemp = typeof tableRow.readOnly === "boolean" && tableRow.readOnly;
        }
        if(!readOnlyTemp) {
            readOnlyTemp = typeof model.readOnly === "boolean" && model.readOnly;
        }
        const isReadOnly = readOnlyTemp;
        let field, value, cell = document.createElement("TD");
        cell.setAttribute("class", "data-table-td");
        if ("icon" === model.type) {
            field = document.createElement("BUTTON");
            field.setAttribute("class", "btn btn-link text-center");
            field.style.textAlign = "center";
            if (isReadOnly) {
                field.setAttribute("disabled", "true");
            }
        } else {
            field = document.createElement("INPUT");
            field.setAttribute("class", "data-table-input");
        }
        if (isReadOnly) {
            field.setAttribute("readonly", "true");
        }
        row.appendChild(cell);
        cell.appendChild(field);
        if ("int" === model.type || "decimal" === model.type || "currency" === model.type || "percentage" === model.type) {
            field.style.textAlign = "right";
            if ("int" === model.type) {
                field.setAttribute("type", "number");
            }
        } else if ("checkbox" === model.type || "boolean" === model.type) {
            field.setAttribute("type", "checkbox");
            field.addEventListener("change", function (evt) {
                evt.preventDefault();
                if (!isReadOnly) {
                    self.pvtOnValueChanged(tableRow.rowId, colIndex, field, field.checked);
                }
            });
        } else if ("range" === model.type) {
            field.setAttribute("type", "range");
            field.setAttribute("value", '0');
        } else if ("button" === model.type || "icon" === model.type) {
            field.setAttribute("type", "button");
            field.setAttribute("class", "data-table-input");
            field.addEventListener("click", function (evt) {
                evt.preventDefault();
                if (typeof self.onActionEvent === "function") {
                    self.onActionEvent(model.name, tableRow, colIndex, evt);
                }
            });
        } else if ("time" === model.type) {
            field.setAttribute("type", model.type);
            field.addEventListener('change', function(evt) {
                evt.preventDefault();
                if (!isReadOnly) {
                    self.pvtOnValueChanged(tableRow.rowId, colIndex, field, field.value);
                }
            });
        }
        value = DCapture.getDeepValue(tableRow.data, model.name);
        value = DCapture.typeSafe(model, tableRow.data, value);
        if (self.isSourceFromCsv) {
            tableRow.values.push(DCapture.typeSafe(model, tableRow.data));
            field.setAttribute("data-input", "modified");
        } else {
            tableRow.values.push(value);
        }
        tableRow.fields.push(field);
        if ("checkbox" === model.type || "boolean" === model.type) {
            field.checked = value;
        } else {
            const tagName = field.tagName.toUpperCase();
            let displayText;
            if(typeof model.getText === "function") {
                displayText = model.getText(model, tableRow.data, value);
            } else {
                displayText = DataTableUtil.getText(model, tableRow.data, value);
            }
            if ("INPUT" === tagName) {
                field.value = displayText;
            } else if ("icon" === model.type) {
                field.innerHTML = displayText;
            } else {
                field.innerText = displayText;
            }
        }
        if (isReadOnly) {
            return true;
        }
        if ("currency" === model.type) {
            field.addEventListener("focus", function (evt) {
                evt.preventDefault();
                const amt = DataTableUtil.parseCurrency(field.value, DataTableUtil.toCurrencyObject(model, tableRow.data));
                if (0 === amt) {
                    field.value = "";
                } else {
                    field.value = amt;
                }

            });
        } else if ("percentage" === model.type) {
            field.addEventListener("focus", function (evt) {
                evt.preventDefault();
                field.value = field.value.replace('%', '');
            });
        } else if ("date" === model.type) {
            $(field).datepicker({
                format: 'yyyy-mm-dd',
                autoHide: true,
                zIndex: 2048
            });
            $(field).on('change', function () {
                self.pvtOnValueChanged(tableRow.rowId, colIndex, field, DCapture.parseDate(field.value.trim()));
            });
        } else if ("decimal" === model.type) {
            field.addEventListener("keyup", function (evt) {
                evt.preventDefault();
                if("" !== field.value.trim()) {
                    field.value = field.value.trim().replace ( /[^0-9.]/g, '');
                }
            });
        }
        if (self.onKeyEvent && "boolean" !== model.type && "checkbox" !== model.type && "range" !== model.type) {
            field.addEventListener("keyup", function (evt) {
                self.onKeyEvent(model.name, tableRow, colIndex, evt);
            });
        }
        if(model.isDisableBlurEvent) {
            return true;
        }
        field.addEventListener("blur", function (evt) {
            evt.preventDefault();
            let newVal;
            if ("currency" === model.type) {
                newVal = DataTableUtil.parseCurrency(field.value.trim(), DataTableUtil.toCurrencyObject(model, tableRow.data));
            } else {
                newVal = DCapture.typeSafe(model, tableRow.data, field.value.trim());
            }
            self.pvtOnValueChanged(tableRow.rowId, colIndex, field, newVal);
        });
    };
    self.executeRenderer = function () {
        if(0 === self.getRowCount() || !Array.isArray(self.renderer) || 0 === self.renderer.length) {
            return;
        }
        const keyArray = [], map = new Map(), tableData = self.getData();
        for(let rx = 0; rx < self.renderer.length; rx++) {
            const rendObj = self.renderer[rx], name = rendObj['name'];
            let hasKey = keyArray.indexOf(name);
            if(-1 === hasKey) {
                keyArray.push(name);
                map.set(name, new Set());
            }
        }
        for(let idx = 0; idx < tableData.length; idx++) {
            let obj = tableData[idx];
            map.forEach((valueSet, key) => {
                if(typeof obj[key] === "string") {
                    map.get(key).add(obj[key]);
                }
            });
        }
        for(let rix = 0; rix < self.renderer.length; rix++) {
            const rendObj = self.renderer[rix], name = rendObj['name'];
            const fkSet = map.get(name);
            if(0 < fkSet.size) {
                rendObj.loadReference(name, Array.from(fkSet));
            }
        }
    }
    self.executeRowRenderer = function (row) {
        if(typeof row !== "object" || typeof self.renderer === "undefined") {
            return;
        }
        const keyArray = [], map = new Map();
        for(let rx = 0; rx < self.renderer.length; rx++) {
            const rendObj = self.renderer[rx], name = rendObj['name'];
            if(typeof rendObj['key'] !== "string") {
                rendObj.key = "fk";
            }
            let hasKey = keyArray.indexOf(name);
            if(-1 === hasKey) {
                keyArray.push(name);
                map.set(name, new Set());
            }
        }
        let obj = row.data;
        map.forEach((valueSet, key) => {
            if(typeof obj[key] === "string") {
                valueSet.add(obj[key]);
            }
        });
        for(let rix = 0; rix < self.renderer.length; rix++) {
            const rendObj = self.renderer[rix], name = rendObj['name'];
            const fkSet = map.get(name);
            if(0 < fkSet.size) {
                rendObj.loadReference(name, Array.from(fkSet), row);
            }
        }
    }
    self.insertRow = function (data, isAppendRow) {
        if (typeof data !== "object") {
            data = {};
        }
        const rowId = self.pvtNextRowId();
        let tableRow, row = document.createElement("TR");
        row.setAttribute("class", 'data-table-tr');
        row.setAttribute("data-id", rowId);
        if (typeof isAppendRow === "boolean" && isAppendRow) {
            self.body.appendChild(row);
        } else {
            self.body.insertBefore(row, self.body.firstChild);
        }
        tableRow = new DataTableRow(rowId, self.columns.length, data, self.isSourceFromCsv ? "modified" : "info");
        tableRow.row = row;
        const isEditableRow = (typeof self.isRowEditable === "function") ? self.isRowEditable(tableRow) : true;
        self.pvtAddFirstCell(row, rowId);
        tableRow.readOnly = !isEditableRow;
        self.tableRows.set(rowId, tableRow);
        self.setRowStatus(row, tableRow.getStatus());
        for (let idx = 0; idx < self.columns.length; idx++) {
            self.appendCell(row, tableRow, idx);
        }
        return tableRow;
    };
    self.setRowData = function (tableRowOrId, data, silently) {
        if (typeof data !== "object" || data === null) {
            return;
        }
        let tableRow = tableRowOrId;
        if (typeof tableRowOrId === "string") {
            if (self.tableRows.has(tableRowOrId)) {
                tableRow = self.tableRows.get(tableRowOrId);
            } else {
                self.insertRow(data);
                tableRow = false;
            }
        }
        if (tableRow instanceof DataTableRow) {
            let modifiedArray = [];
            for (let idx = 0; idx < self.columns.length; idx++) {
                const model = self.columns[idx];
                let status = "info", field, value = DCapture.getDeepValue(data, model.name);
                value = DCapture.typeSafe(model, data, value);
                const hasEmpty = DCapture.isEmpty(model.type, value);
                const hasModified = DCapture.isModified(model.type, tableRow.values[idx], value);
                if (hasModified) {
                    modifiedArray.push(idx);
                }
                if (model.required && hasEmpty) {
                    status = "error";
                } else if (hasModified) {
                    status = "modified";
                }
                tableRow.values[idx] = value;
                tableRow.status[idx] = status;
                field = tableRow.fields[idx];
                field.setAttribute("data-input", status);
                if ("checkbox" === model.type || "boolean" === model.type) {
                    field.checked = value;
                } else {
                    const tagName = field.tagName.toUpperCase();
                    if ("INPUT" === tagName) {
                        field.value = DataTableUtil.getText(model, data, value);
                    } else if ("icon" === model.type) {
                        field.innerHTML = model.html;
                    } else {
                        field.innerText = DataTableUtil.getText(model, data, value);
                    }
                }
            }
            if (0 !== modifiedArray.length) {
                self.setRowStatus(tableRow['row'], tableRow.getStatus());
            }
            const fireEvent = typeof silently !== "boolean" || !silently;
            if (fireEvent && 0 !== modifiedArray.length && (typeof self.onValueChanged === "function")) {
                self.onValueChanged(tableRow, -1);
            }
            self.executeRowRenderer(tableRow);
        }
    };
    self.setData = function (array) {
        self.clearAll();
        self.isSourceFromCsv = false;
        if (Array.isArray(array) && 0 < array.length) {
            for (let idx = 0; idx < array.length; idx++) {
                self.insertRow(array[idx], true);
            }
        }
        self.executeRenderer();
    };
    self.setCsv = function (records) {
        self.clearAll();
        let array = DCapture.parseCsv(records, self.columns);
        self.isSourceFromCsv = true;
        for (let idx = 0; idx < array.length; idx++) {
            self.insertRow(array[idx]);
        }
        return array;
    };
}

function LookupField() {

}

LookupField.pvtSetValue = function (field, text) {
    if (field instanceof jQuery) {
        let tag = field.prop("tagName");
        if (typeof tag === "string") {
            if ("INPUT" === tag.toUpperCase()) {
                field.val(text);
            } else {
                field.text(text);
            }
        }
    } else if (typeof field === "object") {
        let tag = field["tagName"];
        if (typeof tag === "string") {
            if ("INPUT" === tag.toUpperCase()) {
                field.value = text;
            } else {
                field.innerText = text;
            }
        }
    }
};
LookupField.setValue = function (config, data, silently) {
    let idx, items = [];
    if (typeof config.field === "string") {
        config.field = $("#" + config.field);
    }
    config.selected = data;
    if (typeof config.onSelected === "function" && !silently) {
        config.onSelected(data);
    }
    if (data === null || typeof data === "undefined") {
        LookupField.pvtSetValue(config.field, "");
        return;
    }
    if (typeof config.display === "undefined") {
        config.display = [];
        for (idx = 0; idx < config.columns.length; idx++) {
            config.display.push(idx);
        }
    }
    for (idx = 0; idx < config.display.length; idx++) {
        let mdl, txt, value, modelIndex = config.display[idx];
        mdl = config.columns[modelIndex];
        if (typeof mdl === "undefined") {
            throw "LookupField display model should not be null";
        }
        value = DCapture.getDeepValue(data, mdl.name);
        value = DCapture.typeSafe(mdl, data, value);
        txt = DataTableUtil.getText(mdl, data, value);
        if (0 < txt.length) {
            items.push(txt);
        }
    }
    LookupField.pvtSetValue(config.field, items.join(" - "));
};
LookupField.appendRow = function (config, data) {
    let idx, row = $("<tr></tr>");
    LookupField.lookupTable.append(row);
    for (idx = 0; idx < config.columns.length; idx++) {
        let value, mdl = config.columns[idx], td = $("<td></td>");
        row.append(td);
        value = DCapture.getDeepValue(data, mdl.name);
        value = DCapture.typeSafe(mdl, data, value);
        td.text(DataTableUtil.getText(mdl, data, value));
    }
    row.on("click", function (evt) {
        evt.preventDefault();
        LookupField.hide();
        LookupField.setValue(config, data);
    });
    return row;
};
LookupField.hide = function () {
    LookupField.init();
    LookupField.dialog.hide();
    LookupField.wrapper.hide();
    LookupField.lookupTable.empty();
};
LookupField.setData = function (config, data) {
    LookupField.init();
    let array;
    if (typeof data !== "object") {
        data = {};
    } else if(Array.isArray(data)) {
        array = data;
        data = {};
    } else {
        array = data[config.name];
    }
    if (!Array.isArray(array)) {
        array = [];
    }
    LookupField.lookupTable.empty();
    for (let idx = 0; idx < array.length; idx++) {
        LookupField.appendRow(config, array[idx]);
    }
    if (config.paging instanceof Paging) {
        config.paging.set(data);
    }
};
LookupField.load = function (config, req) {
    if (typeof req !== "object") {
        req = config.paging.get(true);
    }
    if (typeof config.filter === "function") {
        let filterObj = config.filter();
        if(typeof filterObj === "object") {
            req = DCapture.merge(req, filterObj);
        }
    }
    if (typeof config.lookup_url === "string") {
        remoteCall({
            url: config.lookup_url,
            data: req,
            error: function (msg) {
                alert(msg.responseText);
            },
            success: function (data) {
                LookupField.setData(config, data);
            }
        });
    } else if (typeof config['loadRecords'] === "function") {
        config.loadRecords();
    }
};
LookupField.init = function () {
    if(typeof LookupField.dialog === "undefined") {
        LookupField.dialog = $("#lookup-dialog");
        LookupField.lookupTable = $("#lookup-table");
        LookupField.searchField = $("#lookup-search");
        LookupField.clearButton = $("#lookup-clear");
        LookupField.closeButton = $("#lookup-close");
        LookupField.wrapper =  $("#lookup-dialog-wrapper");
        LookupField.nextButton = $("#lookup-next");
        LookupField.previousButton = $("#lookup-previous");
        LookupField.wrapper.unbind().on("click", function (evt) {
            evt.preventDefault();
            if (evt.target.id === "lookup-dialog-wrapper") {
                LookupField.hide();
            }
            return false;
        });
        LookupField.closeButton.unbind().on("click", function (evt) {
            evt.preventDefault();
            LookupField.hide();
            return false;
        });
    }
};
LookupField.show = function (config, ele, data) {
    LookupField.init();
    LookupField.lookupTable.empty();
    let wrapHeight = $(document).height(), wrapWidth = $(document).width();
    let height = LookupField.dialog.height(), width = LookupField.dialog.width();
    let rect = typeof ele === "undefined" ? null : ele.getBoundingClientRect();
    if (rect === null) {
        rect = {};
        rect.top = (wrapHeight / 3) - (height);
        rect.left = (wrapWidth / 2) - (width / 2);
    }
    if (rect.top > (wrapHeight - height)) {
        rect.top = wrapHeight - height;
    }
    if (rect.left > (wrapWidth - width)) {
        rect.left = wrapWidth - width;
    }
    LookupField.wrapper.css({height: wrapHeight, width: wrapWidth}).show();
    LookupField.dialog.css({top: rect.top, left: rect.left}).show();
    LookupField.searchField.unbind().on('keyup', function (evt) {
        if (13 === evt.keyCode) {
            let req = config.paging.get(true);
            const srhTxt = LookupField.searchField.val().trim();
            if(0 < srhTxt.length) {
                req.searchText = srhTxt;
            }
            LookupField.load(config, req);
        }
    });
    LookupField.clearButton.unbind().on('click', function (evt) {
        evt.preventDefault();
        LookupField.hide();
        LookupField.setValue(config);
    });
    LookupField.nextButton.unbind();
    LookupField.previousButton.unbind();
    if (typeof config.paging === "undefined") {
        config.paging = new Paging(100);
    }
    config.paging.build("lookup-previous", "lookup-next", "lookup-info");
    config.paging.onNext(function (req) {
        const srhTxt = LookupField.searchField.val().trim();
        if(0 < srhTxt.length) {
            req.searchText = srhTxt;
        }
        LookupField.load(config, req);
    });
    config.paging.onPrevious(function (req) {
        const srhTxt = LookupField.searchField.val().trim();
        if(0 < srhTxt.length) {
            req.searchText = srhTxt;
        }
        LookupField.load(config, req);
    });
    if(typeof data === "object" || Array.isArray(data)) {
        LookupField.setData(config, data);
    } else {
        LookupField.load(config, config.paging.get(true));
    }
    LookupField.searchField.focus();
    LookupField.searchField.select();
};

function ListField() {

}

ListField.pvtSetValue = function (field, text) {
    if (field instanceof jQuery) {
        let tag = field.prop("tagName");
        if (typeof tag === "string") {
            if ("INPUT" === tag.toUpperCase()) {
                field.val(text);
            } else {
                field.text(text);
            }
        }

    } else if (typeof field === "object") {
        let tag = field["tagName"];
        if (typeof tag === "string") {
            if ("INPUT" === tag.toUpperCase()) {
                field.value = text;
            } else {
                field.innerText = text;
            }
        }
    }
};
ListField.setValue = function (config, rowObj, silently) {
    let idx, items = [];
    if (typeof config.columns === "undefined") {
        config.columns = [{name: "value"}];
    }
    if (typeof config.field === "string") {
        config.field = $("#" + config.field);
    }
    config.selected = rowObj;
    if (typeof config.onSelected === "function" && !silently) {
        config.onSelected(rowObj);
    }
    if (rowObj === null || typeof rowObj === "undefined") {
        ListField.pvtSetValue(config.field, "");
        return;
    }
    if (typeof config.display === "undefined") {
        config.display = [];
        for (idx = 0; idx < config.columns.length; idx++) {
            config.display.push(idx);
        }
    }
    for (idx = 0; idx < config.display.length; idx++) {
        let mdl, txt, value, modelIndex = config.display[idx];
        mdl = config.columns[modelIndex];
        if (typeof mdl === "undefined") {
            throw "ListField display model should not be null";
        }
        value = DCapture.getDeepValue(rowObj, mdl.name);
        value = DCapture.typeSafe(mdl, rowObj, value);
        txt = DataTableUtil.getText(mdl, rowObj, value);
        if (0 < txt.length) {
            items.push(txt);
        }
    }
    ListField.pvtSetValue(config.field, items.join(" - "));
};
ListField.appendRow = function (config, rowObj) {
    let idx, row = $("<tr></tr>");
    ListField.listTable.append(row);
    for (idx = 0; idx < config.columns.length; idx++) {
        let value, mdl = config.columns[idx], td = $("<td></td>");
        row.append(td);
        value = DCapture.getDeepValue(rowObj, mdl.name);
        value = DCapture.typeSafe(mdl, rowObj, value);
        td.text(DataTableUtil.getText(mdl, rowObj, value));
    }
    row.on("click", function (evt) {
        evt.preventDefault();
        ListField.hide();
        ListField.setValue(config, rowObj);
    });
    return row;
};
ListField.setData = function (config, array) {
    ListField.init();
    if (!Array.isArray(array)) {
        array = [];
    }
    config.data = array;
    ListField.listTable.empty();
    for (let idx = 0; idx < array.length; idx++) {
        ListField.appendRow(config, array[idx]);
    }
};
ListField.hide = function () {
    ListField.init();
    ListField.dialog.hide();
    ListField.wrapper.hide();
    ListField.listTable.empty();
};
ListField.init = function () {
    if(typeof ListField.dialog === "undefined") {
        ListField.dialog = $("#list-dialog");
        ListField.listTable = $("#list-dialog-table");
        ListField.clearButton = $("#list-dialog-clear");
        ListField.wrapper =  $("#list-dialog-wrapper");
        ListField.wrapper.unbind().on("click", function (evt) {
            evt.preventDefault();
            if (evt.target.id === "lookup-dialog-wrapper") {
                ListField.hide();
            }
            return false;
        });
    }
}
ListField.show = function (config, ele) {
    ListField.init();
    ListField.listTable.empty();
    ListField.clearButton.unbind().on('click', function () {
        ListField.hide();
        ListField.setValue(config);
    });
    if (typeof config.columns === "undefined") {
        config.columns = [{name: "value"}];
    }
    if(Array.isArray(config.master_data) && typeof config.filter === "function") {
        let resultData = [], filCol = config.filter_column, dType = config.filter();
        for(let idx = 0; idx < config.master_data.length; idx++) {
            let filObj = config.master_data[idx];
            if(dType === filObj[filCol]) {
                resultData.push(filObj);
            }
        }
        ListField.setData(config, resultData);
    } else {
        ListField.setData(config, config.data);
    }
    let wrapHeight = $(document).height(), wrapWidth = $(document).width();
    let height = ListField.dialog.height(), width = ListField.dialog.width();
    let rect = typeof ele === "undefined" ? null : ele.getBoundingClientRect();
    if (rect === null) {
        rect = {};
        rect.top = (wrapHeight / 3) - (height);
        rect.left = (wrapWidth / 2) - (width / 2);
    }
    if (rect.top > (wrapHeight - height)) {
        rect.top = wrapHeight - height;
    }
    if (rect.left > (wrapWidth - width)) {
        rect.left = wrapWidth - width;
    }
    ListField.wrapper.css({height: wrapHeight, width: wrapWidth}).show();
    ListField.dialog.css({top: rect.top, left: rect.left}).show();
    ListField.wrapper.unbind().on("click", function (evt) {
        evt.preventDefault();
        if (evt.target.id === "list-dialog-wrapper") {
            ListField.hide();
        }
        return false;
    });
    if(config['allow_delete']) {
        ListField.clearButton.css('visibility', 'hidden');
    } else {
        ListField.clearButton.unbind().on("click", function (evt) {
            evt.preventDefault();
            ListField.hide();
            ListField.setValue(config);
        });
    }
};
