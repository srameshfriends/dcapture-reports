function DCaptureReportApp() {

}

DCaptureReportApp.showMessage = function (msg) {
    if(typeof msg === "undefined") {
        DCaptureReportApp.messageBox.innerText = '';
    } else if(typeof msg === "object" && msg.responseText) {
        if(msg.responseText.includes('html')) {
            DCaptureReportApp.messageBox.innerHTML = msg.responseText;
        } else {
            DCaptureReportApp.messageBox.innerText = msg.responseText;
        }
    } else if(typeof msg === "string") {
        DCaptureReportApp.messageBox.innerText = msg;
    } else {
        DCaptureReportApp.messageBox.innerText = msg.toString();
    }
};

DCaptureReportApp.init = function () {
    DCapture.getDCapture(true);
    DCaptureReportApp.messageBox = document.getElementById("message-box");
    const jasperReports = new JasperReports();
    jasperReports.init();
}
