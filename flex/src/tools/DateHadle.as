/*
 YJ Add
 日期的一些相关操作

 */
package tools {
import mx.formatters.DateFormatter;
import mx.resources.ResourceManager;
import mx.utils.ObjectUtil;

public class DateHadle {
    private static var now:Date = new Date();

    public function DateHadle(dnow:Date = null) {
        if (dnow != null) {
            now = dnow;
        }
    }

    public static function getYear():String {
        return now.fullYear + "";
    }

    public static function getMonth():String {
        return (now.month + 1).toString();
    }

    public static function getFirstOfYear():String {// 取本年的第一天
        return now.fullYear + "-" + "01-01";
    }

    public static function getEndOfYear():String {// 取本年的最后天
        return now.fullYear + "-" + "12-31";
    }

    public static function getFristOfQuatrer():String {
        var mon:int = (Math.ceil((new Date().month + 1) / 3) - 1) * 3 + 1;
        return now.fullYear + "-" + mon + "-01";
    }

    public static function getFirstOfMonth(_now:Date=null):String {// 取本月的第一天
        if(_now==null)
            _now = now;

        var tp:String = "";
        var cur:int = _now.getMonth() + 1;
        if (cur <= 9) {
            tp = "0" + cur;
        } else {
            tp = "" + cur;
        }
        return _now.fullYear + "-" + tp + "-01";
    }

    public static function getEndOfMonth(_now:Date=null):String {// 取本月的最后天
        if(_now==null)
            _now = now;

        var startDate:Date = new Date(_now.getFullYear(), _now.getMonth() + 1);
        var endDate:Date = new Date(startDate.getTime());
        endDate['date'] += -1;
        var FormatDate:DateFormatter = new DateFormatter();
        // 定义日期格式
        FormatDate.formatString = "YYYY-MM-DD";
        return FormatDate.format(endDate);
    }

    public static function getFirstOfWeek():String {// 取本星期的第一天
        var week:Number = now.getDay();
        if (week == 0)
            week = 7;
        var endDate:Date = new Date(now.getTime());
        endDate['date'] = now.getDate() - week + 1;
        var FormatDate:DateFormatter = new DateFormatter();
        // 定义日期格式
        FormatDate.formatString = "YYYY-MM-DD";
        return FormatDate.format(endDate);
    }

    public static function getEndOfWeek():String {// 取本星期的最后一天
        var week:Number = now.getDay();
        if (week == 0)
            week = 7;
        var endDate:Date = new Date(now.getTime());
        endDate['date'] = now.getDate() - week + 7;
        var FormatDate:DateFormatter = new DateFormatter();
        // 定义日期格式
        FormatDate.formatString = "YYYY-MM-DD";
        return FormatDate.format(endDate);
    }


    public static function myDateCompare(dtstart:String, dtend:String):Boolean {

        var dbegin:Date = stringToDate(dtstart, "YYYY-MM-DD");
        var dend:Date = stringToDate(dtend, "YYYY-MM-DD");

        if (ObjectUtil.dateCompare(dbegin, dend) == 1) return false;

        return true;
    }

    //计算日期相差的天数
    public static function myDateDiffer(dtstart:String, dtend:String):int {

        var dbegin:Date = stringToDate(dtstart, "YYYY-MM-DD");
        var dend:Date = stringToDate(dtend, "YYYY-MM-DD");

        var num1:Number = dbegin.valueOf();
        var num2:Number = dend.valueOf();

        var different:Number = num2 - num1;

        var day:int = int(different / 24 / 60 / 60 / 1000);//方法

        return day;
    }

    public static function getToday():String {// 取今天
        var fmt:DateFormatter = new DateFormatter();
        fmt.formatString = "YYYY-MM-DD";
        return fmt.format(now);
    }

    public static function getYestoday():String {// 取昨天
        return getYearMonDay(now);
    }

    private static function getYearMonDay(date:Date):String {
        var strYMD:String;
        var strMon:String;
        var strDay:String;
        if (date == null) {
            strYMD = "2020-12-31";
        }
        else {
            if ((date.getMonth() + 1) < 10) {
                strMon = "0" + (date.getMonth() + 1).toString();
                if ((date.getDate() - 1) < 10 && (date.getDate() - 1) > 0)
                    strDay = "0" + (date.getDate() - 1).toString();
                else if (date.getDate() - 1 == 0) {
                    switch (strMon) {
                        case "01":
                            strDay = "31";
                            strMon = "12";
                            break;
                        case "02":
                            strDay = "31";
                            strMon = "01";
                            break;
                        case "03":
                            strDay = "28";
                            strMon = "02";
                            break;
                        case "04":
                            strDay = "31";
                            strMon = "03";
                            break;
                        case "05":
                            strDay = "30";
                            strMon = "04";
                            break;
                        case "06":
                            strDay = "31";
                            strMon = "05";
                            break;
                        case "07":
                            strDay = "30";
                            strMon = "06";
                            break;
                        case "08":
                            strDay = "31";
                            strMon = "07";
                            break;
                        case "09":
                            strDay = "31";
                            strMon = "08";
                            break;
                        case "10":
                            strDay = "30";
                            strMon = "09";
                            break;
                        case "11":
                            strDay = "31";
                            strMon = "10";
                            break;
                        case "12":
                            strDay = "30";
                            strMon = "11";
                            break;
                    }
                }
                else
                    strDay = (date.getDate() - 1).toString();
            }

            else {
                strMon = (date.getMonth() + 1).toString();
                if ((date.getDate() - 1) < 10 && (date.getDate() - 1) > 0)
                    strDay = "0" + (date.getDate() - 1).toString();
                else if (date.getDate() - 1 == 0) {
                    switch (strMon) {
                        case "01":
                            strDay = "31";
                            strMon = "12";
                            break;
                        case "02":
                            strDay = "31";
                            strMon = "01";
                            break;
                        case "03":
                            strDay = "28";
                            strMon = "02";
                            break;
                        case "04":
                            strDay = "31";
                            strMon = "03";
                            break;
                        case "05":
                            strDay = "30";
                            strMon = "04";
                            break;
                        case "06":
                            strDay = "31";
                            strMon = "05";
                            break;
                        case "07":
                            strDay = "30";
                            strMon = "06";
                            break;
                        case "08":
                            strDay = "31";
                            strMon = "07";
                            break;
                        case "09":
                            strDay = "31";
                            strMon = "08";
                            break;
                        case "10":
                            strDay = "30";
                            strMon = "09";
                            break;
                        case "11":
                            strDay = "31";
                            strMon = "10";
                            break;
                        case "12":
                            strDay = "30";
                            strMon = "11";
                            break;
                    }
                }
                else
                    strDay = (date.getDate() - 1).toString();

            }
            strYMD = date.getFullYear().toString() + "-" + strMon + "-" +
                    strDay;
        }
        return strYMD;
    }

    public static function stringToDate(valueString:String, inputFormat:String):Date {
        var maskChar:String
        var dateChar:String;
        var dateString:String;
        var monthString:String;
        var yearString:String;
        var dateParts:Array = [];
        var maskParts:Array = [];
        var part:int = 0;
        var length:int;
        var position:int = 0;

        if (valueString == null || inputFormat == null)
            return null;

        var monthNames:Array = ResourceManager.getInstance()
                .getStringArray("SharedResources", "monthNames");

        var noMonths:int = monthNames.length;
        for (var i:int = 0; i < noMonths; i++) {
            valueString = valueString.replace(monthNames[i], (i + 1).toString());
            valueString = valueString.replace(monthNames[i].substr(0, 3), (i + 1).toString());
        }

        length = valueString.length;

        dateParts[part] = "";
        for (i = 0; i < length; i++) {
            dateChar = valueString.charAt(i);

            if (isNaN(Number(dateChar)) || dateChar == " ") {
                part++;
                dateParts[part] = dateChar;
                part++;
                dateParts[part] = "";
            }
            else {
                dateParts[part] += dateChar;
            }
        }

        length = inputFormat.length;
        part = -1;
        var lastChar:String;

        for (i = 0; i < length; i++) {
            maskChar = inputFormat.charAt(i);

            if (maskChar == "Y" || maskChar == "M" || maskChar == "D") {
                if (maskChar != lastChar) {
                    part++;
                    maskParts[part] = "";
                }
                maskParts[part] += maskChar;
            }
            else {
                part++;
                maskParts[part] = maskChar;
            }

            lastChar = maskChar;
        }

        length = maskParts.length;

        if (dateParts.length != length) {
            if (valueString.length != inputFormat.length) {
                return null;
            }

            for (i = 0; i < length; i++) {
                dateParts[i] = valueString.substr(position, maskParts[i].length);
                position += maskParts[i].length;
            }

        }

        if (dateParts.length != length) {
            return null;
        }

        for (i = 0; i < length; i++) {
            maskChar = maskParts[i].charAt(0);

            if (maskChar == "D") {
                dateString = dateParts[i];
            }
            else if (maskChar == "M") {
                monthString = dateParts[i];
            }
            else if (maskChar == "Y") {
                yearString = dateParts[i];
            }
        }

        if (dateString == null || monthString == null || yearString == null)
            return null;

        var dayNum:Number = Number(dateString);
        var monthNum:Number = Number(monthString);
        var yearNum:Number = Number(yearString);

        if (isNaN(yearNum) || isNaN(monthNum) || isNaN(dayNum))
            return null;

        if (yearString.length == 2)
            yearNum += 2000;

        var newDate:Date = new Date(yearNum, monthNum - 1, dayNum);

        if (dayNum != newDate.getDate() || (monthNum - 1) != newDate.getMonth())
            return null;

        return newDate;
    }
}


}