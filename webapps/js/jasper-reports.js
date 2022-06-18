function JasperReports() {
    const self = this;
    self.deleteJasperReport = function (reportName) {
        remoteCall({
            url: "/dcapture-reports/jasper/delete?report_name=" + reportName,
            error: function (msg) {
                DCaptureReportApp.showMessage(msg);
            },
            success: function (msg) {
                DCaptureReportApp.showMessage(msg);
                self.loadJasperReportList();
            }
        });
    };
    self.onDeleteEvent = function (evt) {
        evt.preventDefault();
        let array = self.dataTable.getSelected(), text = "You are really wish to delete, the selected jasper report?";
        if(0 < array.length && confirm(text) === true) {
            let value = array[0];
            self.deleteJasperReport(value['report_name']);
        }
    };
    self.saveJasperReport = function (reportName, dataFormat) {
        remoteCall({
            url: "/dcapture-reports/jasper/save?report_name=" + reportName,
            data: dataFormat,
            error: function (msg) {
                DCaptureReportApp.showMessage(msg);
            },
            success: function (msg) {
                DCaptureReportApp.showMessage(msg);
                self.loadJasperReportList();
            }
        });
    };
    self.onSaveEvent = function (evt) {
        evt.preventDefault();
        let array = self.dataTable.getModified();
        if(0 < array.length) {
            let value = array[0];
            self.saveJasperReport(value['report_name'], value['data_format']);
        }
    };
    self.loadJasperReportList = function () {
        remoteCall({
            url: "/dcapture-reports/jasper/loadAll",
            type:'GET',
            error: function (msg) {
                DCaptureReportApp.showMessage(msg);
            },
            success: function (dataArray) {
                self.dataTable.setData(dataArray);
            }
        });
    };
    self.onRefreshEvent = function (evt) {
        evt.preventDefault();
        remoteCall({
            url: "/dcapture-reports/jasper/refresh",
            type:'GET',
            error: function (msg) {
                DCaptureReportApp.showMessage(msg);
                self.loadJasperReportList();
            },
            success: function (msg) {
                DCaptureReportApp.showMessage(msg);
                self.loadJasperReportList();
            }
        });
    };
    self.init = function () {
        self.refreshBtn = document.getElementById("actionRefresh");
        self.saveBtn = document.getElementById("actionSave");
        self.deleteBtn = document.getElementById("actionDelete");
        self.dataTable = new DataTable();
        self.dataTable.build({
            parent: "jasper-reports-table-div",
            columns: [
                {name: "report_name", title:'Report Name', orderBy: true, width:'30%'},
                {name: "data_format", title: 'Format'}
            ]
        });
        self.refreshBtn.addEventListener('click', self.onRefreshEvent);
        self.saveBtn.addEventListener('click', self.onSaveEvent);
        self.deleteBtn.addEventListener('click', self.onDeleteEvent);
        self.loadJasperReportList();
        const params = new URLSearchParams(document.location.search), msg = params.get("message");
        if(typeof msg === "string") {
            DCaptureReportApp.showMessage(msg);
        }
    };
}