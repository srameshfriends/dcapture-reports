function JasperReports() {
}

JasperReports.show = function (name) {
    if(typeof JasperReports.viewMap === "undefined") {
        JasperReports.viewMap = new Map();
        JasperReports.show('jasper-report-update');
        JasperReports.show('jasper-report-form');
        JasperReports.show('jasper-report-list');
    }
    let element = JasperReports.viewMap.get(name);
    if(typeof element === "undefined") {
        element = document.getElementById(name);
        if(typeof element === "object") {
            JasperReports.viewMap.set(name, element);
        }
    }
    JasperReports.viewMap.forEach (function(node, key) {
        if(typeof node === "object" && node !== null) {
            if(name === key) {
                node.style.display = 'block';
            } else {
                node.style.display = 'none';
            }
        }
    });
}

JasperReports.showMessage = function (msg) {
    if(typeof JasperReports.messageBox === "undefined") {
        JasperReports.messageBox = document.getElementById("message-box");
    }
    if(typeof msg === "undefined" || msg === null) {
        JasperReports.messageBox.innerText = '';
    } else if(typeof msg === "object") {
        if(typeof msg.responseText === "object") {
            msg = msg.responseText;
        }
        if(msg.description) {
            if(msg.description.includes('html')) {
                JasperReports.messageBox.innerHTML = msg.description;
            } else {
                JasperReports.messageBox.innerText = msg.description;
            }
        } else if(msg.responseText) {
            if(msg.responseText.includes('html')) {
                JasperReports.messageBox.innerHTML = msg.responseText;
            } else {
                JasperReports.messageBox.innerText = msg.responseText;
            }
        } else {
            JasperReports.messageBox.innerText = msg.toString();
        }
    } else {
        JasperReports.messageBox.innerText = msg.toString();
    }
};

JasperReports.initUpdateView = function () {
    DCapture.getDCapture(true);

};

function JasperReportList() {
    const self = this;
    self.deleteJasperReport = function (array) {
        remoteCall({
            url: "/jasper/source/delete",
            type: "DELETE",
            data: array,
            error: function (msg) {
                JasperReports.showMessage(msg);
            },
            success: function (msg) {
                JasperReports.showMessage(msg);
                self.loadJasperReportList();
            }
        });
    };
    self.onDeleteEvent = function (evt) {
        evt.preventDefault();
        let array = self.dataTable.getSelected(), text = "You are really wish to delete, the selected jasper report?";
        if (0 < array.length && confirm(text) === true) {
            self.deleteJasperReport(array);
        }
    };
    self.loadJasperReportList = function () {
        remoteCall({
            url: "/jasper/source/loadAll",
            type: 'GET',
            error: function (msg) {
                JasperReports.showMessage(msg);
            },
            success: function (dataArray) {
                self.dataTable.setData(dataArray);
                JasperReports.showMessage("");
            }
        });
    };
    self.onReloadEvent = function (evt) {
        evt.preventDefault();
        remoteCall({
            url: "/jasper/reload",
            type: 'GET',
            error: function (msg) {
                JasperReports.showMessage(msg);
                self.loadJasperReportList();
            },
            success: function (msg) {
                JasperReports.showMessage(msg);
                self.loadJasperReportList();
            }
        });
    };
    self.init = function () {
        self.reloadBtn = document.getElementById("actionReloadReport");
        self.dataTable = new DataTable();
        self.dataTable.build({
            parent: "jasper-reports-table-div",
            readOnly: true,
            columns: [
                {name: "report_name", title: 'Name', type: "button", orderBy: true, width: '25%'},
                {name: "report_title", title: 'Title', orderBy: true, width: '25%'},
                {name: "data_format", title: 'Format'}
            ],
            onActionEvent: function (actionId, tableRow) {
                if ("report_name" === actionId) {
                    JasperReports.jasperReportUpdate.setData(tableRow.data);
                    JasperReports.show('jasper-report-update');
                }
            }
        });
        self.reloadBtn.addEventListener('click', self.onReloadEvent);
        self.loadJasperReportList();
        const params = new URLSearchParams(document.location.search), msg = params.get("message");
        if (typeof msg === "string") {
            JasperReports.showMessage(msg);
        }
    };
}

function JasperReportUpdate() {
    const self = this;
    self.setData = function (data) {
        self.reportName.value = data.report_name;
        self.reportNameDisplay.innerText = data.report_name;
        self.dataFormat.innerText = data.data_format;
        self.reportTitle.value = data.report_title;
        self.sourceCreatedOn.innerText = data.jasper_class_modified;
        self.lastUpdatedOn.innerText = data.updated_on;
    };
    self.init = function () {
        const updateRemoteCall = function (req) {
            remoteCall({
                url: "/jasper/source/update",
                type: "PUT",
                data: req,
                error: function (msg) {
                    JasperReports.showMessage(msg);
                },
                success: function () {
                    JasperReports.show('jasper-report-list');
                    JasperReports.reload();
                }
            });
        };
        const deleteRemoteCall = function (reportName) {
            remoteCall({
                url: "/jasper/source/delete?report_name=" + reportName,
                type: "DELETE",
                contentType: "text",
                error: function (msg) {
                    JasperReports.showMessage(msg);
                },
                success: function () {
                    JasperReports.show('jasper-report-list');
                    JasperReports.reload();
                }
            });
        };
        self.actionReportUpdate = document.getElementById("actionReportUpdate");
        self.reportName = document.getElementById("report_name");
        self.reportNameDisplay = document.getElementById("report_name_display");
        self.reportTitle = document.getElementById("report_title");
        self.dataFormat = document.getElementById("data_format");
        self.sourceCreatedOn = document.getElementById("source_created_on");
        self.lastUpdatedOn = document.getElementById("last_updated_on");
        self.actionReportDelete = document.getElementById("actionReportDelete");
        self.actionReportUpdate.addEventListener('click', function (evt){
            evt.preventDefault();
            const reportName = self.reportName.value, reportTitle = self.reportTitle.value.trim();
            if(reportTitle === "") {
                alert("Report title should not be empty.");
                return;
            }
            const dataFormat = self.dataFormat.innerText.trim();
            if(dataFormat === "" || !dataFormat.startsWith("{")) {
                alert("Report data format should not be empty.");
                return;
            }
            if (confirm("You are really wish to update, the selected jasper report?") === true) {
                updateRemoteCall({
                    report_name:reportName, report_title: reportTitle, data_format:dataFormat
                });
            }
        });
        self.actionReportDelete.addEventListener('click', function (evt) {
            evt.preventDefault();
            if(confirm("You are really wish to delete, the selected jasper report?") === true) {
                deleteRemoteCall(self.reportName.value);
            }
        });
        self.showListViewBtn = document.getElementById("showListViewBtn");
        self.showListViewBtn.addEventListener('click', function (evt) {
            evt.preventDefault();
            JasperReports.show('jasper-report-list');
            JasperReports.reload();
        });
    };
}

JasperReports.reload = function () {
    JasperReports.jasperReportList.loadJasperReportList();
}

JasperReports.initialize = function () {
    DCapture.getDCapture(true);
    const urlParams = new URLSearchParams(window.location.search);
    JasperReports.showMessage(urlParams.get('msg'));
    const addReportLink = document.getElementById('actionAddReport');
    addReportLink.addEventListener('click', function (evt) {
        evt.preventDefault();
        JasperReports.show('jasper-report-form');
    });
    const actionShowReportLink = document.getElementById('actionShowReportList');
    actionShowReportLink.addEventListener('click', function (evt) {
        evt.preventDefault();
        JasperReports.show('jasper-report-list');
    });
    JasperReports.show('jasper-report-list');
    JasperReports.jasperReportList = new JasperReportList();
    JasperReports.jasperReportList.init();
    JasperReports.jasperReportUpdate = new JasperReportUpdate();
    JasperReports.jasperReportUpdate.init();
}
