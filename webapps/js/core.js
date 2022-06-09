/*
* core.js
* @version : 1.3
* @Created By : Ramesh.S
* */
function DCapture() {

}

DCapture.SECURED = true;

DCapture.MONTHS = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];

DCapture.getCookie = function (name) {
    if (typeof document.cookie !== "undefined") {
        let cookies = document.cookie.split(';');
        for (let idx = 0; idx < cookies.length; idx++) {
            let pair = cookies[idx].trim().split('=');
            if (name === pair[0]) {
                return pair[1];
            }
        }
    }
};

DCapture.setCookie = function (name, text, days) {
    if (typeof document.cookie !== "undefined") {
        if (typeof days !== "number") {
            days = 1;
        }
        let dte = new Date();
        dte.setTime(dte.getTime() + (days * 24 * 60 * 60 * 1000));
        const expires = "expires=" + dte.toUTCString();
        document.cookie = name + "=" + text + "; SameSite=Strict;Secure " + expires + ";path=/";
        return true;
    }
};

DCapture.addListener = function (callback) {
    if (typeof callback === "function") {
        DCapture.getDCapture().listeners.push(callback);
    }
};
DCapture.notify = function (name, cfg) {
    const dCap = DCapture.getDCapture();
    for (let idx = 0; idx < dCap.listeners.length; idx++) {
        let callback = dCap.listeners[idx];
        callback(name, cfg);
    }
};
DCapture.base64Key = function () {
    return "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=";
};
DCapture.mask = function () {
    return "8888eff7-5400-40ff-864e-583d1f57490b";
};
DCapture.encodeBase64 = function (string) {
    let i = 0, result = "";
    do {
        let a = string.charCodeAt(i++), b = string.charCodeAt(i++), c = string.charCodeAt(i++);
        a = a ? a : 0;
        b = b ? b : 0;
        c = c ? c : 0;
        let b1 = (a >> 2) & 0x3F, b2 = ((a & 0x3) << 4) | ((b >> 4) & 0xF);
        let b3 = ((b & 0xF) << 2) | ((c >> 6) & 0x3), b4 = c & 0x3F;
        if (!b) {
            b3 = b4 = 64;
        } else if (!c) {
            b4 = 64;
        }
        result += DCapture.base64Key().charAt(b1) + DCapture.base64Key().charAt(b2) + DCapture.base64Key().charAt(b3)
            + DCapture.base64Key().charAt(b4);
    } while (i < string.length);
    return result;
};
DCapture.decodeBase64 = function (string) {
    let i = 0, result = '';
    do {
        let b1 = DCapture.base64Key().indexOf(string.charAt(i++));
        let b2 = DCapture.base64Key().indexOf(string.charAt(i++));
        let b3 = DCapture.base64Key().indexOf(string.charAt(i++));
        let b4 = DCapture.base64Key().indexOf(string.charAt(i++));
        let a = ((b1 & 0x3F) << 2) | ((b2 >> 4) & 0x3), b = ((b2 & 0xF) << 4) | ((b3 >> 2) & 0xF);
        let c = ((b3 & 0x3) << 6) | (b4 & 0x3F);
        result += String.fromCharCode(a) + (b ? String.fromCharCode(b) : '') + (c ? String.fromCharCode(c) : '');
    } while (i < string.length);
    return result;
};
DCapture.encryptAESECBPKCS7 = function (keyText16Char, plainText) {
    if (typeof keyText16Char !== "string") {
        keyText16Char = "ab";
    }
    keyText16Char = keyText16Char + "DCAPTURE5mRAMESH";
    const key = CryptoJS.enc.Utf8.parse(keyText16Char.substr(0, 16));
    const iv1 = CryptoJS.enc.Utf8.parse("hf8685nfhfhjs9h8");
    const encrypted = CryptoJS.AES.encrypt(plainText, key, {
        keySize: 16,
        iv: iv1,
        mode: CryptoJS.mode.ECB,
        padding: CryptoJS.pad.Pkcs7
    });
    return encrypted + "";
}
DCapture.decryptAESECBPKCS7 = function (keyText16Char, cipher) {
    if (typeof keyText16Char !== "string") {
        keyText16Char = "ab";
    }
    keyText16Char = keyText16Char + "DCAPTURE5mRAMESH";
    const key = CryptoJS.enc.Utf8.parse(keyText16Char.substr(0, 16));
    const iv1 = CryptoJS.enc.Utf8.parse("hf8685nfhfhjs9h8");
    const plainText = CryptoJS.AES.decrypt(cipher, key, {
        keySize: 16,
        iv: iv1,
        mode: CryptoJS.mode.ECB,
        padding: CryptoJS.pad.Pkcs7
    });
    return plainText.toString(CryptoJS.enc.Utf8);
}
DCapture.now = function (isTime) {
    let dte = new Date();
    let month = '' + (dte.getMonth() + 1), day = '' + dte.getDate(), year = dte.getFullYear();
    if (month.length < 2) {
        month = '0' + month;
    }
    if (day.length < 2) {
        day = '0' + day;
    }
    if (typeof isTime !== "boolean" || !isTime) {
        return year + "-" + month + "-" + day;
    }
    let hours = dte.getHours(), minutes = dte.getMinutes(), seconds = dte.getSeconds();
    if (hours.length < 2) {
        hours = '0' + hours;
    }
    if (minutes.length < 2) {
        minutes = '0' + minutes;
    }
    if (seconds.length < 2) {
        seconds = '0' + seconds;
    }
    let dateText = [year, month, day].join('-');
    return dateText + " " + hours + ":" + minutes + ":" + seconds;
};
DCapture.getDouble = function (text, currency) {
    let value = 0;
    if (typeof text === "number") {
        value = text;
    } else if (typeof text === "string") {
        value = parseFloat(text);
    } else if (text === null || typeof text === "undefined") {
        return 0;
    }
    if (Number.isNaN(value)) {
        return 0;
    }
    if (typeof currency == "number") {
        return DCapture.toFixed(value, currency);
    } else if (typeof currency == "object") {
        let precision = currency['precision'];
        if (typeof precision !== "number") {
            precision = 4;
        }
        return DCapture.toFixed(value, precision);
    }
    return DCapture.toFixed(value, 4);
};
DCapture.getPrice = function (text, currency) {
    let value = 0;
    if (typeof text === "number") {
        value = text;
    } else if (typeof text === "string") {
        value = parseFloat(text);
    } else if (text === null || typeof text === "undefined") {
        return 0;
    }
    if (Number.isNaN(value)) {
        return 0;
    }
    if (typeof currency == "number") {
        return DCapture.toFixed(value, (currency + 2));
    } else if (typeof currency == "object") {
        let precision = currency['precision'];
        if (typeof precision !== "number") {
            precision = 4;
        }
        return DCapture.toFixed(value, precision + 2);
    }
    return DCapture.toFixed(value, 4);
};
DCapture.getInt = function (text) {
    let value = 0;
    if (typeof text === "number") {
        value = text;
    } else if (typeof text === "string") {
        value = parseInt(text);
    }
    if (Number.isNaN(value)) {
        return 0;
    }
    return value;
};
DCapture.isInteger = function (text) {
    return !/\D/.test(text);
};
DCapture.toFixed = function (num, digits, base) {
    const pow = Math.pow(base || 10, digits);
    return Math.round(num * pow) / pow;
};
DCapture.pvtFormatPattern = function (value, isMinusPrefix, isINRPattern) {
    let text = "";
    if (typeof value === "number") {
        if (isNaN(value) || 0 === value) {
            return "";
        }
        text = value.toString();
    } else if (typeof value === "string") {
        text = value.trim();
    } else {
        return "";
    }
    const isNegative = text.startsWith("-");
    if (isNegative) {
        text = text.substring(1);
    }
    const dotIndex = text.indexOf(".");
    const afterDot = 0 > dotIndex ? "" : text.substring(dotIndex + 1);
    const beforeDot = 0 > dotIndex ? text : text.substring(0, dotIndex);
    let isFirstComma = false, count = 0, item = [];
    if (true === isINRPattern) {
        for (let idx = beforeDot.length; idx > -1; idx--) {
            item.push(beforeDot.charAt(idx));
            if (idx === 0) {
                break;
            }
            count += 1;
            if (isFirstComma && 2 === count) {
                item.push(",");
                count = 0;
            } else if (item.length === 4) {
                item.push(",");
                count = 0;
                isFirstComma = true;
            }
        }
    } else {
        for (let idx = beforeDot.length; idx > -1; idx--) {
            item.push(beforeDot.charAt(idx));
            if (idx === 0) {
                break;
            }
            count += 1;
            if (isFirstComma && 3 === count) {
                item.push(",");
                count = 0;
            } else if (item.length === 4) {
                item.push(",");
                count = 0;
                isFirstComma = true;
            }
        }
    }
    text = item.reverse().join("");
    if (0 < afterDot.length) {
        text = text + "." + afterDot;
    }
    if (isNegative) {
        if (isMinusPrefix) {
            return "-" + text;
        }
        return "(" + text + ")";
    }
    return text;
};
DCapture.isMobileDevice = function () {
    let mode = DCapture.getCookie('screen_view_mode');
    if(typeof mode === "undefined") {
        mode = 'screen.view.default';
    }
    if('screen.view.device' === mode) {
        return true;
    } else if('screen.view.desktop' === mode) {
        return false;
    }
    const toMatch = [
        /Android/i,
        /webOS/i,
        /iPhone/i,
        /iPad/i,
        /iPod/i,
        /BlackBerry/i,
        /Windows Phone/i
    ];
    return toMatch.some((toMatchItem) => {
        return navigator.userAgent.match(toMatchItem);
    });
}
DCapture.removeChildren = function (ele) {
    if(typeof ele === "string") {
        ele = document.getElementById(ele);
    }
    if(typeof ele === "object") {
        while (ele.firstChild) {
            ele.removeChild(ele.lastChild);
        }
    }
}
DCapture.getCurrency = function (value) {
    if (typeof value === "number") {
        let array = DCapture.getValue("currency-list");
        if (Array.isArray(array)) {
            for (let idx = 0; idx < array.length; idx++) {
                let cur = array[idx];
                if (value === cur.id) {
                    return cur;
                }
            }
        }
    } else if (typeof value === "object") {
        let map = DCapture.getValue("currency-map");
        if (map instanceof Map) {
            let cur = map.get(value['code']);
            if (typeof cur === "object") {
                return cur;
            }
        }
    } else if (typeof value === "string") {
        let map = DCapture.getValue("currency-map");
        if (map instanceof Map) {
            let cur = map.get(value);
            if (typeof cur === "object") {
                return cur;
            }
        }
    }
    return DCapture.getValue("default-currency");
};
DCapture.formatCurrency = function (decimal, cfg) {
    if (typeof decimal !== "number" || Number.isNaN(decimal) || decimal === 0) {
        return "";
    }
    const dCap = DCapture.getDCapture();
    let precision = 2, code = "", symbol = "", isMinusPrefix = dCap.isMinusPrefix;
    cfg = DCapture.getCurrency(cfg);
    if (cfg !== null && typeof cfg === "object") {
        if (typeof cfg.code === "string" && 3 === cfg.code.length) {
            code = cfg.code.toUpperCase();
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
    if (3 === code.length && "INR" === code) {
        return symbol + DCapture.pvtFormatPattern(decimal, isMinusPrefix, true);
    }
    return symbol + DCapture.pvtFormatPattern(decimal, isMinusPrefix, false);
};
DCapture.parseCurrency = function (text, cfg) {
    if (text === null || (typeof text === "undefined") || 0 === text.length) {
        return 0.0;
    }
    cfg = DCapture.getCurrency(cfg);
    let precision = 2, symbol = "";
    if (cfg !== null && typeof cfg === "object") {
        if (typeof cfg.symbol === "string" && 4 > cfg.symbol.length) {
            symbol = cfg.symbol;
        }
        if (typeof cfg.precision === "number") {
            if (-1 < cfg.precision && 9 > cfg.precision) {
                precision = cfg.precision;
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
DCapture.merge = function (targetObj, sourceObj) {
    if (typeof sourceObj === "object") {
        for (let key in sourceObj) {
            if (sourceObj.hasOwnProperty(key)) {
                targetObj[key] = sourceObj[key];
            }
        }
    }
    return targetObj;
};
DCapture.generateUUID = function () { // Public Domain/MIT
    let d = new Date().getTime();
    if (typeof performance !== 'undefined' && typeof performance.now === 'function') {
        d += performance.now(); //use high-precision timer if available
    }
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
        let r = (d + Math.random() * 16) % 16 | 0;
        d = Math.floor(d / 16);
        return (c === 'x' ? r : (r & 0x3 | 0x8)).toString(16);
    });
};
DCapture.copy = function (source) {
    if (Object.prototype.toString.call(source) === '[object Array]') {
        let i, clone = [];
        for (i = 0; i < source.length; i++) {
            clone[i] = DCapture.copy(source[i]);
        }
        return clone;
    } else if (typeof (source) === "object") {
        let cloned = {};
        for (let prop in source) {
            if (source.hasOwnProperty(prop)) {
                cloned[prop] = DCapture.copy(source[prop]);
            }
        }
        return cloned;
    } else {
        return source;
    }
};
DCapture.filter = function (data, req) { // todo review
    let rowIdx, colIdx, filteredArray = [];
    if (typeof req.searchText === "string" && 0 < req.searchText.trim().length) {
        let searchText = req.searchText.trim().toUpperCase();
        for (rowIdx = 0; rowIdx < data.length; rowIdx++) {
            let row = data[rowIdx];
            for (colIdx = 0; colIdx < row.length; colIdx++) {
                let value = row[colIdx].toUpperCase();
                if (colIdx < row.length && -1 < value.indexOf(searchText)) {
                    filteredArray.push(row);
                }
            }
        }
        req.length = filteredArray.length;
    } else {
        filteredArray = data.slice(req.start, req.limit);
        req.length = filteredArray.length;
    }
    return filteredArray;
};
DCapture.parseCsv = function (records, columns) {
    if (!Array.isArray(records) || 2 > records.length) {
        return [];
    }
    let models = new Map(), result = [], header = records[0], data = records.slice(1, records.length - 1);
    for (let ix = 0; ix < columns.length; ix++) {
        const mdl = columns[ix];
        for (let jx = 0; jx < header.length; jx++) {
            if (mdl.name === header[jx]) {
                models.set(jx, {name: mdl.name, type: mdl.type});
            }
        }
    }
    for (let rw = 0; rw < data.length; rw++) {
        let obj = {}, values = data[rw];
        models.forEach(function (model, index) {
            if (index < values.length) {
                DCapture.setDeepValue(obj, model, values[index]);
            }
        });
        result.push(obj);
    }
    return result;
};
DCapture.typeSafe = function (model, data, value) {
    if (typeof model.typeSafe === "function") {
        return model.typeSafe(model, data, value);
    }
    const dType = model['type'];
    if (typeof dType === "undefined" || dType === null) {
        return value;
    } else if (typeof value === "undefined" || value === null) {
        if ("string" === dType || "date" === dType) {
            return null;
        } else if ("int" === dType) {
            return 0;
        } else if ("decimal" === dType || "currency" === dType || "percentage" === dType) {
            return 0.0;
        } else if ("boolean" === dType || "checkbox" === dType) {
            return false;
        } else if ("range" === dType) {
            return 0;
        } else if ("text" === dType) {
            return null;
        }
        return null;
    }
    const valueType = typeof value;
    if (dType === valueType) {
        if ("int" === dType) {
            return Math.round(value);
        }
        return value;
    } else if ("string" === dType) {
        if (valueType === "string") {
            return 0 === value.trim().length ? null : value.trim();
        }
        return value.toString();
    } else if ("int" === dType) {
        const numV = parseInt(value);
        return isNaN(numV) ? 0 : Math.round(numV);
    } else if ("decimal" === dType) {
        let dmlVal = parseFloat(value);
        if (isNaN(dmlVal) || 0 === dmlVal) {
            return 0.0;
        }
        if (typeof model.precision !== "number") {
            model.precision = 4;
        }
        return DCapture.toFixed(dmlVal, model.precision);
    } else if ("currency" === dType) {
        if ("string" === valueType) {
            return DCapture.parseCurrency(value);
        }
        let dmlVal = parseFloat(value);
        if (isNaN(dmlVal) || 0 === dmlVal) {
            return 0.0;
        }
        if (typeof model.precision !== "number") {
            model.precision = 4;
        }
        return DCapture.toFixed(dmlVal, model.precision);
    } else if ("boolean" === dType || "checkbox" === dType) {
        let boolVal = value.toString().toLowerCase();
        return "true" === boolVal;
    } else if ("range" === dType) {
        if (typeof value === "number") {
            return value;
        }
        let ranValue = parseInt(value);
        return isNaN(ranValue) ? 0 : ranValue;
    } else if ("text" === dType) {
        if (valueType === "string") {
            return value.trim();
        }
        return value.toString();
    } else if ("percentage" === dType) {
        if (valueType === "string") {
            let percent = value.replace('%', '');
            percent = parseFloat(percent.trim());
            return isNaN(percent) ? 0 : percent;
        } else if (valueType === "number") {
            return isNaN(value) ? 0 : value;
        }
        return 0;
    }
    return value;
};
DCapture.getDeepValue = function (data, name) {
    if (typeof data !== "object" || data === null) {
        return;
    }
    name = name.trim();
    const index = name.indexOf(".");
    if (-1 === index) {
        return data[name];
    } else {
        const prefix = name.substr(0, index);
        const suffix = index < name.length ? name.substr(index + 1, name.length) : false;
        if (!suffix) {
            return data[prefix];
        } else if (typeof data[prefix] === "object") {
            return DCapture.getDeepValue(data[prefix], suffix);
        } else {
            return data[prefix];
        }
    }
};
DCapture.setDeepValue = function (data, model, value) {
    if (typeof data === "object" && data !== null) {
        const name = model.name.trim();
        const index = name.indexOf(".");
        if (-1 === index) {
            data[name] = DCapture.typeSafe(model, data, value);
        } else {
            const prefix = name.substr(0, index);
            const suffix = index < name.length ? name.substr(index + 1, name.length) : false;
            let nextObj = data[prefix];
            if (typeof nextObj === "undefined" || nextObj === null) {
                nextObj = {};
                data[prefix] = nextObj;
            }
            if (suffix === false) {
                if (typeof value !== "object") {
                    data[prefix] = DCapture.typeSafe(model, data, value);
                } else {
                    data[prefix] = DCapture.typeSafe(model, data, value.toString());
                }
            } else if (-1 === suffix.indexOf(".")) {
                if (typeof value === "object" && value !== null) {
                    nextObj[suffix] = DCapture.typeSafe(model, data, value[suffix]);
                } else {
                    nextObj[suffix] = DCapture.typeSafe(model, data, value);
                }
            } else {
                const nextPrefix = suffix.substr(0, suffix.indexOf("."));
                if (typeof value === "undefined" || value === null) {
                    nextObj[nextPrefix] = {};
                } else {
                    const hasValue = value[nextPrefix];
                    let nextModel = {name: suffix, type: model.type, precision: model.precision};
                    if (typeof hasValue === "object" && hasValue !== null) {
                        DCapture.setDeepValue(nextObj, nextModel, hasValue);
                    } else {
                        DCapture.setDeepValue(nextObj, nextModel, value);
                        nextObj[nextPrefix] = {};
                    }
                }
            }
        }
    }
};
DCapture.getDateText = function (date) {
    if (date instanceof Date) {
        let month = DCapture.MONTHS[date.getMonth()], day = '' + date.getDate();
        if (day.length < 2) {
            day = '0' + day;
        }
        return date.getFullYear() + "-" + month + "-" + day;
    } else if (typeof date === "string" && 9 < date.length) {
        let spaceChar1 = date.substring(4, 5), spaceChar2 = date.substring(7, 8);
        if (spaceChar1 === spaceChar2) {
            const day = date.substring(8, 10), year = parseInt(date.substring(0, 4));
            let month = parseInt(date.substring(5, 7));
            month = DCapture.MONTHS[month - 1];
            return year + "-" + month + "-" + day;
        } else if (11 < date.length) {
            return date.substring(0, 11);
        }
        return date;
    }
    return "";
};
DCapture.formatDate = function (date) {
    if (date instanceof Date) {
        let month = date.getMonth() + 1, day = date.getDate();
        if (10 > day) {
            day = '0' + day;
        }
        if (10 > month) {
            month = '0' + month;
        }
        return date.getFullYear() + "-" + month + "-" + day;
    } else if (typeof date === "string" && 9 < date.length) {
        let spaceChar1 = date.substring(4, 5), spaceChar2 = date.substring(7, 8);
        if (spaceChar1 === spaceChar2) {
            let month, day = parseInt(date.substring(8, 10)), year = parseInt(date.substring(0, 4));
            month = parseInt(date.substring(5, 7)) + 1;
            if (10 > day) {
                day = '0' + day;
            }
            if (10 > month) {
                month = '0' + month;
            }
            return year + "-" + month + "-" + day;
        } else if (11 < date.length) {
            let month, day = parseInt(date.substring(9, 11)), year = parseInt(date.substring(0, 4));
            month = date.substring(5, 8).toLowerCase();
            let monthIndex = -1;
            for (let idx = 0; idx < 12; idx++) {
                if (month === DCapture.MONTHS[idx].toLowerCase()) {
                    monthIndex = idx;
                    break;
                }
            }
            if (0 > monthIndex) {
                return "";
            }
            monthIndex = monthIndex + 1;
            if (10 > day) {
                day = '0' + day;
            }
            if (10 > monthIndex) {
                monthIndex = '0' + monthIndex;
            }
            return year + "-" + monthIndex + "-" + day;
        }
        return date;
    }
    return "";
};
DCapture.parseDate = function (text) {
    if (typeof text === "undefined" || text === null) {
        return null;
    } else if (text instanceof Date) {
        return text;
    } else if ((typeof text === "string")) {
        if (10 === text.length) {
            const day = parseInt(text.substring(8, 10)), month = text.substring(5, 7),
                year = parseInt(text.substring(0, 4));
            return new Date(year, month - 1, day, 0, 0, 0, 0);
        } else if (11 === text.length) {
            const day = parseInt(text.substring(9, 11)), year = parseInt(text.substring(0, 4));
            let mi, month = text.substring(5, 8).toUpperCase();
            for (mi = 0; mi < 12; mi++) {
                if (month === DCapture.MONTHS[mi].toUpperCase()) {
                    month = mi;
                    break;
                }
            }
            if (typeof month === "number") {
                return new Date(year, month, day, 0, 0, 0, 0);
            }
        }
    }
    return null;
};
DCapture.findCurrency = function (model, data) {
    let result = false;
    if (typeof data === "object" && typeof model.reference === "string") {
        result = data[model.reference];
    }
    if (!result) {
        if (typeof model.currency === "function") {
            result = model.currency();
        } else if (typeof model.currency === "object") {
            result = model.currency;
        }
    }
    if (typeof result === "object") {
        return result;
    } else if (typeof result === "number") {
        return result;
    }
    return result ? result : DCapture.getCurrency("default-currency");
};
DCapture.getText = function (model, data, value) {
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
        return DCapture.formatCurrency(value, DCapture.findCurrency(model, data));
    } else if ("date" === model.type) {
        return DCapture.getDateText(value);
    } else if ("boolean" === model.type || "checkbox" === model.type) {
        return value;
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
    } else if ("range" === model.type) {
        if (typeof value === "number") {
            return value;
        }
        let range = parseInt(value);
        if (isNaN(range)) {
            return 0;
        }
        return range;
    } else if ("text" === model.type) {
        if (typeof value === "string") {
            return value.replace("\r\n", "\\r\\n");
        }
        return value.toString();
    }
    return value.toString();
};
DCapture.isEmpty = function (type, value) {
    if (typeof value === "undefined" || value === null) {
        return true;
    }
    if ("int" === type || "decimal" === type) {
        if (isNaN(value) || 0 === value) {
            return true;
        }
    }
    if ("string" === type || "text" === type) {
        if (0 === value.length) {
            return true;
        }
    }
    return false;
};
DCapture.isModified = function (type, oldValue, newValue) {
    let obj1 = typeof oldValue === "undefined" || oldValue === null;
    let obj2 = typeof newValue === "undefined" || newValue === null;
    if (obj1 !== obj2) {
        return true;
    }
    obj1 = typeof oldValue;
    obj2 = typeof newValue;
    if (obj1 !== obj2) {
        return true;
    }
    return oldValue !== newValue;
};
DCapture.clearData = function () {
    if (localStorage) {
        localStorage.clear();
    }
}
DCapture.removeData = function (name) {
    if (localStorage) {
        localStorage.removeItem(name);
    }
};
DCapture.setData = function (name, value) {
    if (localStorage) {
        if (typeof value === "undefined") {
            return;
        }
        if (typeof value === "object") {
            localStorage.setItem(name, JSON.stringify(value));
        } else {
            localStorage.setItem(name, value);
        }
    } else {
        alert("Local storage not supported : (" + name + " : " + value + ")");
    }
};
DCapture.getData = function (name) {
    if (localStorage) {
        return localStorage.getItem(name);
    } else {
        alert("Local storage not supported : " + name);
    }
};
DCapture.getDataObject = function (name) {
    let txt = DCapture.getData(name);
    if (!(typeof txt === "undefined" || typeof txt !== "string" || 0 === txt.length)) {
        try {
            return JSON.parse(txt);
        } catch (ex) {
            alert(ex + ", local storage text parsing error \n" + txt);
        }
    }
};

DCapture.pvtStorageDataType = function () {
    let dataTypeObj = sessionStorage.getItem('DCAPTURE_DATA_TYPE');
    if(dataTypeObj == null || typeof dataTypeObj === "undefined") {
        dataTypeObj = '{}';
        sessionStorage.setItem('DCAPTURE_DATA_TYPE', dataTypeObj);
    }
    return JSON.parse(dataTypeObj);
};

DCapture.pvtGetStorageDataType = function (name) {
    let dataTypeObj = DCapture.pvtStorageDataType();
    return dataTypeObj[name];
};

DCapture.pvtSetStorageDataType = function (name, dataType) {
    let dataTypeObj = DCapture.pvtStorageDataType();
    dataTypeObj[name] = dataType;
    sessionStorage.setItem('DCAPTURE_DATA_TYPE', JSON.stringify(dataTypeObj));
};

DCapture.clearValue = function () {
    if (DCapture.SECURED) {
        DCapture.pvtClearValue();
    } else if (sessionStorage) {
        sessionStorage.clear();
    }
};

DCapture.removeValue = function (name) {
    if (DCapture.SECURED) {
        DCapture.pvtRemoveValue(name);
    } else if (sessionStorage) {
        sessionStorage.removeItem(name);
        let dataTypeObj = DCapture.pvtStorageDataType();
        delete dataTypeObj[name];
        sessionStorage.setItem('DCAPTURE_DATA_TYPE', JSON.stringify(dataTypeObj));
    }
};
DCapture.setValue = function (name, value, duration) {
    if (DCapture.SECURED) {
        DCapture.pvtSetValue(name, value, duration);
    } else if (sessionStorage) {
        if (typeof value === "undefined" || value == null) {
            DCapture.removeValue(name);
        } else if (value instanceof Date) {
            DCapture.pvtSetStorageDataType(name, 'date');
            sessionStorage.setItem(name, value.toString());
        } else if (typeof value === "string" || typeof value === "number" || typeof value === "boolean" || typeof value === "bigint") {
            DCapture.pvtSetStorageDataType(name, typeof value);
            sessionStorage.setItem(name, value);
        } else if (typeof value === "object") {
            DCapture.pvtSetStorageDataType(name, "object");
            sessionStorage.setItem(name, JSON.stringify(value));
        } else {
            alert("Session storage data type (" + typeof value + ") not yet supported ("
                + name + " : " + value + ")");
        }
        if (99 < duration) {
            const delayId = window.setInterval(function () {
                clearInterval(delayId);
                DCapture.removeValue(name);
            }, duration);
        }
    } else {
        alert("Session storage not supported (" + name + " : " + value + ")");
    }
};
DCapture.getValue = function (name) {
    if (DCapture.SECURED) {
        return DCapture.pvtGetValue(name);
    } else if (sessionStorage) {
        let dataType = DCapture.pvtGetStorageDataType(name), text = sessionStorage.getItem(name);
        if(typeof text === "undefined" || text == null) {
            return;
        }
        if('object' === dataType) {
           return JSON.parse(text);
        } else if('number' === dataType) {
            return parseFloat(text);
        } else if('boolean' === dataType) {
            return 'true' === text.toLowerCase();
        } else if('date' === dataType) {
            return new Date(text);
        }
        return text;
    } else {
        alert("Session storage not supported : " + name);
    }
};

DCapture.message = function (name) {
    let txt, data = DCapture.getValue("messages");
    if (typeof data === "object") {
        txt = data[name];
    }
    return (typeof txt === "string" && 0 < txt.trim().length) ? txt : name;
};
DCapture.messages = function (array) {
    if (!Array.isArray(array)) {
        return [];
    }
    let data = DCapture.getValue("messages");
    if (typeof data !== "object") {
        return array;
    }
    let result = [], x;
    for (x = 0; x < array.length; x++) {
        let txt = data[array[x]];
        if ((typeof txt === "string" && 0 < txt.trim().length)) {
            result[x] = txt;
        } else {
            result[x] = array[x];
        }
    }
    return result;
};
DCapture.messageFormat = function (name, args) {
    let msg = DCapture.message(name);
    if (name === msg || typeof args === "undefined") {
        return msg;
    }
    for (let idx = 0; idx < args.length; idx++) {
        msg = msg.replace("{" + idx + "}", args[idx]);
    }
    return msg;
};
DCapture.pvtRemoveValue = function (name) {
    const dCap = DCapture.getDCapture();
    if (typeof dCap.memory === "undefined") {
        dCap.memory = new Map();
    }
    dCap.memory.delete(name);
};
DCapture.pvtSetValue = function (name, data, duration) {
    const dCap = DCapture.getDCapture();
    dCap.memory.set(name, data);
    if (99 < duration) {
        const delayId = window.setInterval(function () {
            clearInterval(delayId);
            DCapture.removeValue(name);
        }, duration);
    }
};
DCapture.pvtClearValue = function () {
    DCapture.getDCapture().memory = new Map();
};
DCapture.pvtGetValue = function (name) {
    return DCapture.getDCapture().memory.get(name);
};
DCapture.loadMessage = function (url, callback) {
    remoteCall({
        url: url,
        type: "GET",
        error: function () {
            console.log("Message Loading Error : " + url);
            callback();
        },
        success: function (msgObject) {
            let result = DCapture.getValue("messages");
            if (typeof result !== "object") {
                result = {};
            }
            Object.assign(result, msgObject);
            DCapture.setValue("messages", result);
            callback();
        }
    });
}
DCapture.getMessageBundle = function (resolve) {
    DCapture.loadMessage("../api/shared/messages/load", function () {
        resolve();
    });
};
DCapture.addMessage = function (name, value) {
    if (typeof DCapture.getValue("messages") !== "object") {
        DCapture.setValue("messages", {});
    }
    DCapture.getValue("messages")[name] = value;
};
DCapture.showPreviousHtml = function () {
    let dCap = DCapture.getDCapture();
    if (typeof dCap.previousView.href === "string" && 4 < dCap.previousView.href.length) {
        DCapture.showHtml(dCap.previousView.href);
    }
};
DCapture.getAccessibleList = function (moduleFeatureOnly) {
    if (typeof moduleFeatureOnly !== "boolean") {
        moduleFeatureOnly = false;
    }
    let dCap = DCapture.getDCapture();
    if (Array.isArray(dCap.accessible)) {
        let accessibleMap = new Map(), array = dCap.accessible;
        for (let idx = 0; idx < array.length; idx++) {
            let obj = array[idx];
            accessibleMap.set(obj.feature, obj);
        }
        let resultList = [];
        if (moduleFeatureOnly) {
            dCap.modules.forEach(function (mdl, mdlName) {
                for (let ix = 0; ix < mdl.features.length; ix++) {
                    let feature = mdl.features[ix];
                    if (!feature.hidden && accessibleMap.has(feature.name)) {
                        let result = accessibleMap.get(feature.name);
                        result.module = mdlName;
                        resultList.push(result);
                    }
                }
            });
        } else {
            accessibleMap.forEach(function (mdl) {
                resultList.push(mdl);
            });
        }
        return resultList;
    }
};
DCapture.getDCapture = function (isResetValues) {
    if(typeof window.dcapture === "undefined" || true === isResetValues) {
        window.dcapture = {};
        window.dcapture.modules = new Map();
        window.dcapture.isDebugEnabled = true;
        window.dcapture.previousView = {};
        window.dcapture.listeners = [];
        window.dcapture.isMessageDialogAppended = false;
        window.dcapture.memory = new Map();
        window.dcapture.isMinusPrefix = false;
        window.dcapture.application = {};
        window.dcapture.application.features = [];
        window.dcapture.application.featureMap = new Map();
        window.dcapture.accessible = [];
    }
    return window.dcapture;
};
DCapture.getFeatureMap = function () {
    return DCapture.getDCapture().application.featureMap;
};
DCapture.setAccessible = function (accessibleMap) {
    let featureArray = DCapture.getDCapture().application.getFeatures(), featureMap = new Map();
    for(let idx = 0; idx < featureArray.length; idx++) {
        let item = featureArray[idx];
        featureMap.set(item.name, item);
    }
    featureMap.forEach(function (target) {
        let source = accessibleMap.get(target.name);
        if (typeof source === "object") {
            DCapture.merge(target, source);
        }
    });
    DCapture.getDCapture().application.featureMap = featureMap;
    return featureMap;
};
DCapture.getAccessible = function (name, role) {
    if (typeof role !== "string") {
        role = "view";
    }
    let dCap = DCapture.getDCapture(), model = dCap.application.featureMap.get(name);
    if (typeof model !== "object") {
        alert("Feature : " + name + " not found");
    }
    return model[role];
};

DCapture.isDebug = function () {
    return typeof dcapture === "object" && dcapture.isDebugEnabled;
};

DCapture.history = {
    getParameter : function () {
        let param = window.location.search;
        if (0 === param.trim().length) {
            return {};
        }
        if (param.startsWith("?")) {
            param = param.substring(1);
        }
        if (param.trim().length) {
            return param.trim();
        }
    },
    getURL : function () {
        let urlSplit = (window.location.href).split("?"), url = urlSplit[0];
        if(urlSplit.length) {
            return urlSplit[0];
        }
    },
    set: function (viewId, args, title) {
        let param = '', url = this.getURL();
        if (typeof args === "object" && args !== null) {
            param = JSON.stringify(args);
        }
        param = window.btoa(viewId + "@" + param);
        title = typeof title !== 'undefined' ? title : document.title;
        history.pushState(param, title, url + "?" + param);
    },
    get: function () {
        let param = this.getParameter(), viewId = false, obj = false, temp = false;
        if (typeof param !== "string" || 0 === param.trim().length) {
            return [];
        }
        try {
            param = window.atob(param.trim());
            let inx = param.indexOf('@');
            if(1 > inx) {
                return [];
            }
            viewId = param.substring(0, inx);
            temp = param.substring(inx + 1);
            if(1 < temp.length) {
                obj = JSON.parse(temp);
            }
            if(typeof obj === "object") {
                return [viewId, obj];
            }
        } catch (e) {
            console.log(e);
        }
        return viewId ? [viewId] : [];
    },
    generate: function (id, args) {
        let result = {id: id};
        if (typeof args === "undefined" || args === null) {
            args = {};
        }
        result.data = args;
        return window.btoa(JSON.stringify(result));
    }
};
DCapture.switchViewMode = function () {
    const dCap = DCapture.getDCapture(), apps = dCap.application;
    if (typeof apps.isViewRendered === "function") {
        dCap.isViewRendered = apps.isViewRendered();
    } else {
        dCap.isViewRendered = false;
    }
    let features = apps.getFeatures(), featuresMap = new Map();
    if(DCapture.isMobileDevice()) {
        let deviceFeatureMap = new Map(), deviceFeatureList = apps.getDeviceFeatures();
        if(typeof deviceFeatureList === "object" && Array.isArray(deviceFeatureList)) {
            for (let ix = 0; ix < deviceFeatureList.length; ix++) {
                let devFtr = deviceFeatureList[ix];
                deviceFeatureMap.set(devFtr.name, devFtr);
            }
        }
        for (let idx = 0; idx < features.length; idx++) {
            let obj = features[idx];
            if(deviceFeatureMap.has(obj.name)) {
                let dfo = deviceFeatureMap.get(obj.name);
                if(Array.isArray(dfo.href)) {
                    obj.href = dfo.href;
                }
                if(typeof dfo.viewId === "string") {
                    obj.viewId = dfo.viewId;
                }
            }
            featuresMap.set(obj.name, obj);
        }
    } else {
        for (let idx = 0; idx < features.length; idx++) {
            let obj = features[idx];
            featuresMap.set(obj.name, obj);
        }
    }
    dCap.application.features = featuresMap;
};
DCapture.setApplication = function (apps, isDebugEnabled) {
    const dCap = DCapture.getDCapture();
    if(isDebugEnabled) {
        DCapture.clearData();
    }
    fetch('../version.json').then(res => res.json()).then((out) => {
        let oldVersion = DCapture.getData('html@version');
        if (out.version !== oldVersion) {
            DCapture.clearData();
        }
        DCapture.setData('html@version', out.version);
    }).catch(err => { console.log(err);});
    dCap.isDebugEnabled = typeof isDebugEnabled === "boolean" && isDebugEnabled;
    dCap.application = apps;
    DCapture.setAccessible(new Map());
    DCapture.switchViewMode();
};
DCapture.pvtClearMain = function (model) {
    const dCap = DCapture.getDCapture();
    let mainNode = dCap.application.mainNode;
    let titleNode = dCap.application.titleNode;
    while (mainNode.firstChild) {
        mainNode.removeChild(mainNode.firstChild);
    }
    if (typeof model === "object") {
        if (typeof model['titleId'] === "string") {
            model.title = DCapture.message(model['titleId']);
        } else if (typeof model.title !== "string") {
            model.title = DCapture.message(model.name);
        }
        if (titleNode === "object" && titleNode !== null) {
            titleNode.innerText = model.title;
        }
    }
};
DCapture.pvtLoadHtml = function (urls, callback) {
    if (1 === urls.length) {
        remoteCall({
            url: urls[0],
            type: 'GET',
            contentType: 'html/text',
            error: function () {
                console.log('Remote html content not found (' + urls[0] + ")");
            },
            success: function (html) {
                DCapture.setData('html@' + urls[0], html);
                callback(html);
            }
        });
    } else if (1 < urls.length) {
        let array = [];
        for (let idx = 0; idx < urls.length; idx++) {
            array.push({url: urls[idx], type: 'GET', contentType: 'html/text'});
        }
        const multiRemoteCall = new MultiRemoteCall();
        multiRemoteCall.add(array).call(function (result) {
            let textForHtml = '';
            for (let rx = 0; rx < result.length; rx++) {
                const rst = result[rx];
                if (rst.status) {
                    textForHtml = textForHtml + rst.data;
                    DCapture.setData('html@' + rst.url, rst.data);
                }
            }
            if (typeof callback === "function") {
                callback(textForHtml);
            }
        });
    }
};
DCapture.pvtCachedHtml = function (urls, callback, isDebugEnabled) {
    if (isDebugEnabled) {
        DCapture.pvtLoadHtml(urls, callback);
    } else if (1 === urls.length) {
        let cachedHtml = DCapture.getData('html@' + urls[0]);
        if (typeof cachedHtml === "string") {
            callback(cachedHtml);
        } else {
            DCapture.pvtLoadHtml(urls, function (htm) {
                callback(htm);
            });
        }
    } else {
        let array = [], htmlTxt = '';
        for (let idx = 0; idx < urls.length; idx++) {
            let src = urls[idx], txt = DCapture.getData('html@' + src);
            if (typeof txt === "string") {
                htmlTxt = htmlTxt + txt;
            } else {
                array.push(src);
            }
        }
        if (0 === array.length) {
            callback(htmlTxt);
        } else {
            DCapture.pvtLoadHtml(array, function () {
                let htmlTxt2 = '';
                for (let jdx = 0; jdx < urls.length; jdx++) {
                    let rcc = urls[jdx], txt2 = DCapture.getData('html@' + rcc);
                    if (typeof txt2 === "string") {
                        htmlTxt2 = htmlTxt2 + txt2;
                    }
                }
                callback(htmlTxt2);
            });
        }
    }
}
DCapture.pvtDisplayView = function (model, args, silently) {
    const dCap = DCapture.getDCapture();
    if (typeof dCap.application.mainNode === "undefined") {
        alert("Page main node should not be empty");
        return;
    }
    DCapture.pvtClearMain(model);
    let mainNode = dCap.application.mainNode;
    let titleNode = dCap.application.titleNode;
    if (typeof model.viewController !== "object") {
        let viewId = model['viewId'];
        if (typeof viewId === "string") {
            model.viewController = eval('new ' + viewId + '()');
        } else if (dCap.isDebugEnabled) {
            console.log("View id not found for " + model.name);
        }
    }
    if (typeof titleNode === "object" && titleNode !== null) {
        titleNode.innerText = model.title;
    }
    if (!silently) {
        DCapture.history.set(model.name, args, model.title);
    }
    document.title = model.title;
    if (dCap.isDebugEnabled) {
        if (!dCap.isViewRendered) {
            DCapture.pvtCachedHtml(model.href, function (data) {
                mainNode.innerHTML = data;
                if (typeof model.viewController === "object" && typeof model.viewController.init === "function") {
                    model.viewController.init(args);
                    DCapture.notify("ACTIVE_VIEW", model);
                } else if (dCap.isDebugEnabled) {
                    console.log("Page view is not valid [" + model.name + " : " + model.href + "]");
                }
            }, true);
        } else {
            if (typeof model.viewController === "object" && typeof model.viewController.render === "function") {
                model.viewController.render(mainNode, args);
                DCapture.notify("ACTIVE_VIEW", model);
            } else if (dCap.isDebugEnabled) {
                console.log("Page view is not valid [" + model.name + " : " + model.href + "]");
            }
        }
    } else if (!dCap.isViewRendered) {
        let html = DCapture.getData("html@" + model.url);
        if (typeof html === "undefined" || !html) {
            DCapture.pvtCachedHtml(model.href, function (data) {
                mainNode.innerHTML = data;
                if (typeof model.viewController === "object" && typeof model.viewController.init === "function") {
                    model.viewController.init(args);
                    DCapture.notify("ACTIVE_VIEW", model);
                } else if (dCap.isDebugEnabled) {
                    console.log("Page view is not valid [" + model.name + " : " + model.href + "]");
                }
            }, dCap.isDebugEnabled);
        } else {
            mainNode.innerHTML = html;
            if (typeof model.viewController === "object" && typeof model.viewController.init === "function") {
                model.viewController.init(args);
                DCapture.notify("ACTIVE_VIEW", model);
            }
        }
    } else {
        if (typeof model.viewController === "object" && typeof model.viewController.render === "function") {
            model.viewController.render(mainNode, args);
            DCapture.notify("ACTIVE_VIEW", model);
        } else if (dCap.isDebugEnabled) {
            console.log("Page view is not valid [" + model.name + " : " + model.href + "]");
        }
    }
};
DCapture.showHtml = function (url, args) {
    let dCap = DCapture.getDCapture(), model = false;
    dCap.application.features.forEach(value => {
        if (url === value.url) {
            model = value;
        }
    });
    if (typeof model === "object") {
        DCapture.showView(model.name, args);
    }
};
DCapture.showView = function (name, args, silently) {
    const dCap = DCapture.getDCapture();
    let model = dCap.application.features.get(name);
    if (typeof model === "undefined") {
        dCap.application.error(DCapture.message("pageNotFound"));
    } else {
        let isAuthorized = DCapture.getAccessible(model.name, "view");
        if(typeof isAuthorized === "undefined") {
            isAuthorized = false;
        }
        let isSecured = dCap.application.isSecured(model.name, model.secured);
        if (!isAuthorized && isSecured) {
            const msg2 = DCapture.messageFormat("unauthorizedAccessRedirect");
            if (window.confirm(msg2)) {
               DCapture.showView(dCap.application.getSignInView());
            }
            return;
        }
        const redirectView = dCap.application.getForwardView(name, isAuthorized, isSecured);
        if (redirectView === name) {
            dCap.previousView = model;
            DCapture.pvtDisplayView(model, args, silently);
        } else if ("" !== redirectView) {
            DCapture.showView(redirectView);
        }
    }
};
DCapture.updateDataMessage = function () {
    let idx, tagName, name, node, nodeArray = document.querySelectorAll('[data-msg]');
    for (idx = 0; idx < nodeArray.length; idx++) {
        node = nodeArray[idx];
        name = node.getAttribute("data-msg");
        if (typeof name === "string") {
            tagName = node.tagName.toUpperCase();
            if (tagName === "INPUT" || tagName === "TEXTAREA" || tagName === "SELECT") {
                node.setAttribute('placeholder', DCapture.message(name));
            } else {
                node.innerText = DCapture.message(name);
            }
        }
    }
};
DCapture.setUrlParameter = function (parameter) {
    if (history.pushState) {
        let url = window.location.protocol + "//" + window.location.host + window.location.pathname + '?' + parameter;
        window.history.pushState({path: url}, '', url);
    }
};
DCapture.getUrlParameter = function (name) {
    let results, regex, url = window.location.href;
    name = name.toLowerCase();
    url = url.toLowerCase();
    name = name.replace(/[\[\]]/g, '\\$&');
    regex = new RegExp('[?&]' + name + '(=([^&#]*)|&|#|$)'), results = regex.exec(url);
    if (!results) return "";
    if (!results[2]) return "";
    return decodeURIComponent(results[2].replace(/\+/g, ' '));
};
DCapture.isClass = function (ele, className) {
    if (ele.classList) {
        return ele.classList.contains(className);
    }
    return !!ele.className.match(new RegExp('(\\s|^)' + className + '(\\s|$)'));
};
DCapture.addClass = function (ele, className) {
    if (ele.classList) {
        ele.classList.add(className);
    } else if (!DCapture.isClass(ele, className)) {
        ele.className += " " + className;
    }
};
DCapture.removeClass = function (ele, className) {
    if (ele.classList) {
        ele.classList.remove(className);
    } else if (DCapture.isClass(ele, className)) {
        let reg = new RegExp('(\\s|^)' + className + '(\\s|$)');
        ele.className = ele.className.replace(reg, ' ');
    }
};
DCapture.getSize = function () {
    const wi = window, dc = document, ed = dc.documentElement, bd = dc.getElementsByTagName('body')[0];
    const xx = wi.innerWidth || ed.clientWidth || bd.clientWidth;
    const yy = wi.innerHeight || ed.clientHeight || bd.clientHeight;
    return [xx, yy];
};

function Paging(limit) {
    let self = this;
    self.name = "";
    self.offset = 0;
    self.length = 0;
    self.totalRecords = 0;
    self.isPrevious = false;
    self.isNext = false;
    self.orderBy = "";
    self.listener = false;
    self.previous = {};
    self.next = {};
    self.info = {};
    if (typeof limit === "number") {
        if (0 === limit) {
            limit = 10;
        } else if (100 < limit) {
            limit = 100;
        }

    } else {
        limit = 20;
    }
    self.limit = limit;
    self.build = function (previousNode, nextNode, infoNode) {
        if(typeof previousNode === "string") {
            previousNode = document.getElementById(previousNode);
        }
        if(typeof nextNode === "string") {
            nextNode = document.getElementById(nextNode);
        }
        if(typeof infoNode === "string") {
            infoNode = document.getElementById(infoNode);
        }
        self.previous = previousNode;
        self.next = nextNode;
        self.info = infoNode;
        return self;
    };
    self.init = function (callback) {
        self.previous = document.getElementById("actionPrevious");
        self.next = document.getElementById("actionNext");
        self.info = document.getElementById("pagingInfo");
        self.onNext(callback);
        self.onPrevious(callback);
        return self;
    };
    self.set = function (obj) {
        if (typeof obj !== "object") {
            obj = {};
        }
        if (typeof obj.offset !== "number") {
            obj.offset = 0;
        }
        if (typeof obj.size !== "number") {
            obj.size = 0;
        }
        if (typeof obj.totalRecords !== "number") {
            obj.totalRecords = 0;
        }
        self.offset = obj.offset;
        self.totalRecords = obj.totalRecords;
        self.size = obj.size;
        let offsetIdx = obj.offset + 1, endIdx = obj.offset + obj.size, currentCount;
        self.isPrevious = true;
        self.isNext = true;
        if (endIdx === 0) {
            endIdx = obj.totalRecords;
        }
        let infoText = " - ";
        if (0 !== obj.size || 0 !== obj.totalRecords) {
            if (0 === obj.size) {
                if (0 < obj.totalRecords) {
                    infoText = offsetIdx + " - " + endIdx + " of " + obj.totalRecords;
                } else {
                    infoText = offsetIdx + " - " + endIdx;
                }
            } else {
                if (0 < obj.totalRecords) {
                    infoText = offsetIdx + " - " + endIdx + " of " + obj.totalRecords;
                } else {
                    infoText = offsetIdx + " - " + endIdx;
                }
            }
        }
        if (typeof self.info !== "undefined") {
            if (typeof self.info.setAttribute === "function") {
                self.info.innerText = infoText;
            } else if (typeof self.info.text === "function") {
                self.info.text(infoText);
            }
        }
        if (0 === obj.offset) {
            self.isPrevious = false;
        }
        currentCount = obj.offset + obj.size;
        if (obj.totalRecords <= currentCount && -1 !== obj.totalRecords) {
            self.isNext = false;
        }
        let preOpa = self.isPrevious ? 1 : 0.6;
        preOpa = "opacity:" + preOpa + "; -moz-opacity:" + preOpa + "; filter:alpha(opacity=" + (preOpa * 100) + ")";
        let nexOpa = self.isNext ? 1 : 0.6;
        nexOpa = "opacity:" + nexOpa + "; -moz-opacity:" + nexOpa + "; filter:alpha(opacity=" + (nexOpa * 100) + ")";
        if (typeof self.previous.setAttribute === "function") {
            self.previous.setAttribute("style", preOpa);
        } else if (typeof self.previous.attr === "function") {
            self.previous.attr("style", preOpa);
        }
        if (typeof self.next.setAttribute === "function") {
            self.next.setAttribute("style", nexOpa);
        } else if (typeof self.next.attr === "function") {
            self.next.attr("style", nexOpa);
        }
        return {offset: self.offset, limit: self.limit, searchText: ""};
    };
    self.get = function (reset) {
        if (reset === true) {
            self.set({});
        }
        return {offset: self.offset, limit: self.limit, searchText: ""};
    };
    self.getNext = function () {
        let obj = {};
        obj.offset = self.offset + self.limit;
        obj.limit = self.limit;
        return obj;
    };
    self.getPrevious = function () {
        let obj = {};
        obj.offset = self.offset - self.limit;
        obj.offset = 0 > obj.offset ? 0 : obj.offset;
        obj.limit = self.limit;
        return obj;
    };
    self.onNext = function (callback) {
        if (typeof self.next.unbind === "function") {
            self.next.unbind().on("click", function (evt) {
                evt.preventDefault();
                if (self.isNext && (typeof callback === "function")) {
                    callback(self.getNext());
                }
            });
        } else if (typeof self.next.addEventListener === "function") {
            $(self.next).unbind();
            self.next.addEventListener("click", function (evt) {
                evt.preventDefault();
                if (self.isNext && (typeof callback === "function")) {
                    callback(self.getNext());
                }
            });
        }
    };
    self.onPrevious = function (callback) {
        if (typeof self.previous.unbind === "function") {
            self.previous.unbind().on("click", function (evt) {
                evt.preventDefault();
                if (self.isPrevious && (typeof callback === "function")) {
                    callback(self.getPrevious());
                }
            });
        } else if (typeof self.previous.addEventListener === "function") {
            $(self.previous).unbind();
            self.previous.addEventListener("click", function (evt) {
                evt.preventDefault();
                if (self.isPrevious && (typeof callback === "function")) {
                    callback(self.getPrevious());
                }
            });
        }
    };
}

DCapture.getDateRange = function (name) {
    const now = new Date();
    let temp;
    switch (name) {
        case "today":
            return [new Date(), new Date()];
        case "thisMonth":
            temp = new Date();
            temp.setDate(1);
            return [temp, new Date()];
        case "lastMonth":
            temp = new Date();
            temp.setMonth(temp.getMonth() - 1);
            temp.setDate(1);
            return [temp, new Date(now.getFullYear(), now.getMonth(), 0)];
        case "last7Days":
            temp = new Date();
            temp.setDate(temp.getDate() - 7);
            return [temp, now];
        case "last10Days":
            temp = new Date();
            temp.setDate(temp.getDate() - 10);
            return [temp, now];
        case "last15Days":
            temp = new Date();
            temp.setDate(temp.getDate() - 15);
            return [temp, now];
        case "last30Days":
            temp = new Date();
            temp.setMonth(temp.getMonth() - 1);
            return [temp, now];
        case "last60Days":
            temp = new Date();
            temp.setMonth(temp.getMonth() - 2);
            return [temp, now];
        case "last90Days":
            temp = new Date();
            temp.setMonth(temp.getMonth() - 3);
            return [temp, now];
        case "thisYear":
            temp = new Date();
            temp.setMonth(0);
            temp.setDate(1);
            return [temp, now];
        case "lastYear":
            temp = new Date();
            temp.setFullYear(now.getFullYear() - 1);
            temp.setMonth(0);
            temp.setDate(1);
            return [temp, new Date(now.getFullYear(), 0, 0)];
        case "nextYear":
            let frmDate = new Date();
            frmDate.setDate(frmDate.getDate() + 1);
            temp = new Date();
            temp.setFullYear(now.getFullYear() + 1);
            return [frmDate, temp];
    }
    temp = new Date();
    temp.setDate(temp.getDate() - 1);
    return [temp, new Date()];
};

DCapture.formatDateRange = function (selected) {
    let name = "today";
    if (typeof selected === "string") {
        name = selected;
    } else if (typeof selected === "object" && typeof selected.name === "string") {
        name = selected.name;
    }
    let range = DCapture.getDateRange(name);
    let month = range[0].getMonth() + 1, day = range[0].getDate(), yyyy = range[0].getFullYear();
    let month2 = range[1].getMonth() + 1, day2 = range[1].getDate(), yyyy2 = range[1].getFullYear();
    if (10 > day) {
        day = '0' + day;
    }
    if (10 > month) {
        month = '0' + month;
    }
    if (10 > day2) {
        day2 = '0' + day2;
    }
    if (10 > month2) {
        month2 = '0' + month2;
    }
    return [yyyy + "-" + month + "-" + day, yyyy2 + "-" + month2 + "-" + day2];
};

DCapture.getExportName = function (fileFormat) {
    let now = new Date();
    let month = now.getMonth() + 1, day = now.getDate(), yyyy = now.getFullYear(), hour = now.getHours();
    let minutes = now.getMinutes();
    if (10 > day) {
        day = '0' + day;
    }
    if (10 > month) {
        month = '0' + month;
    }
    if (10 > hour) {
        hour = '0' + hour;
    }
    if (10 > minutes) {
        minutes = '0' + minutes;
    }
    return yyyy + month + day + hour + minutes + "." + fileFormat;
};

DCapture.getDateRangeArray = function () {
    let result = DCapture.getValue("date-range-list");
    if (typeof result === "undefined") {
        result = [];
        let msg, dateRange = ["today", "thisMonth", "lastMonth", "last7Days", "last10Days", "last15Days",
            "last30Days", "last60Days", "last90Days", "thisYear", "lastYear", "nextYear"];
        msg = DCapture.messages(dateRange);
        for (let idx = 0; idx < dateRange.length; idx++) {
            result.push({name: dateRange[idx], value: msg[idx]})
        }
        DCapture.setValue("date-range-list", result);
    }
    return result;
};

DCapture.getDateRangeDefault = function () {
    return {name: "last30Days", value: DCapture.message("last30Days")};
};

DCapture.getDateRangeThisYear = function () {
    return {name: "thisYear", value: DCapture.message("thisYear")};
};

DCapture.fireEvent = function (eventName, node) {
    // Make sure we use the ownerDocument from the provided node to avoid cross-window problems
    let doc;
    if (node.ownerDocument) {
        doc = node.ownerDocument;
    } else if (9 === node.nodeType) {
        // the node may be the document itself, nodeType 9 = DOCUMENT_NODE
        doc = node;
    } else {
        throw new Error("Invalid node passed to fireEvent: " + node.id);
    }
    if (node.dispatchEvent) {
        // Gecko-style approach (now the standard) takes more work
        let eventClass = "";
        // Different events have different event classes.
        // If this switch statement can't map an eventName to an eventClass,
        // the event firing is going to fail.
        switch (eventName) {
            case "click": // Dispatching of 'click' appears to not work correctly in Safari. Use 'mousedown' or 'mouseup' instead.
            case "mousedown":
            case "mouseup":
                eventClass = "MouseEvents";
                break;
            case "focus":
            case "change":
            case "blur":
            case "select":
                eventClass = "HTMLEvents";
                break;
            default:
                throw "fireEvent: Couldn't find an event class for event '" + eventName + "'.";
        }
        let bubbles, event = doc.createEvent(eventClass);
        bubbles = "change" !== eventName;
        // All events created as bubbling and cancelable.
        event.initEvent(eventName, bubbles, true);
        event.synthetic = true; // allow detection of synthetic events
        node.dispatchEvent(event);
    } else if (node.fireEvent) {
        // IE-old school style
        let event = doc.createEventObject();
        // allow detection of synthetic events
        event.synthetic = true;
        node.fireEvent("on" + eventName, event);
    }
};
