/**
 * Created by aruis on 14-1-22.
 */
package tools {
import basic.PopUpWindow;

import flash.desktop.NativeApplication;
import flash.display.DisplayObject;
import flash.events.Event;
import flash.external.ExternalInterface;
import flash.net.SharedObject;
import flash.utils.ByteArray;

import mx.collections.ArrayCollection;
import mx.core.FlexGlobals;
import mx.core.IFlexDisplayObject;
import mx.formatters.DateFormatter;
import mx.formatters.NumberBaseRoundType;
import mx.formatters.NumberFormatter;
import mx.managers.PopUpManager;
import mx.rpc.events.ResultEvent;
import mx.utils.StringUtil;

import spark.components.Alert;
import spark.components.gridClasses.GridColumn;

public class CRMtool {
    private static const ALERT_PROMPT:String = "提示";
    private static const ALERT_ERROR:String = "错误";
    private static const ALERT_AFFIRM:String = "确认";

    Alert.buttonHeight = 70;
    Alert.buttonWidth = 130;
    Alert.YES_LABEL = "确定";
    Alert.NO_LABEL = "取消";

    [Embed(source='/assets/swfs/assets.swf', symbol='prompt_png')]
    public static var ALERT_PNG_PROMPT:Class;

    [Embed(source='/assets/swfs/assets.swf', symbol='error_png')]
    public static var ALERT_PNG_ERROR:Class;

    [Embed(source='/assets/swfs/assets.swf', symbol='affirm_png')]
    public static var ALERT_PNG_AFFIRM:Class;

    public function CRMtool() {
    }

    public static function copyArrayCollection(list:ArrayCollection):ArrayCollection {
        return new ArrayCollection(CRMtool.ObjectCopy(list.toArray()));
    }

    public static function ObjectCopy(source:Object):* {
        var myBA:ByteArray = new ByteArray();
        myBA.writeObject(source);
        myBA.position = 0;
        return(myBA.readObject());
    }


    public static function isStringNotNull(str:String):Boolean {
        if (str == null) {
            return false;
        }
        else if (StringUtil.trim(str).length == 0) {
            return false;
        } else if (StringUtil.trim(str) == "null" || StringUtil.trim(str) == "NULL") {
            return false;
        }
        else return true;
    }

    public static function isStringNull(str:String):Boolean {
        if (str == null) {
            return true;
        }
        else if (StringUtil.trim(str).length == 0) {
            return true;
        } else if (StringUtil.trim(str) == "null" || StringUtil.trim(str) == "NULL") {
            return true;
        }
        else return false;
    }

    public static function isNumber(str:*):Boolean {
        if (parseFloat(str + "") + "" == "NaN")
            return false;
        else
            return true;
    }

    public static function getNumber(str:*):* {
        if (CRMtool.isStringNull(str)) {
            return null
        } else if (isNumber(str))
            return parseFloat(str)
        else
            return 0;
    }

    public static function openView(view:IFlexDisplayObject, flag:Boolean = true, p:DisplayObject = null, ismodule:Boolean = false):void {
        if (view) {
            if (p == null) {
                if (ismodule) {
                    p = FlexGlobals.topLevelApplication.moduleInstance as DisplayObject;
                } else {
                    p = FlexGlobals.topLevelApplication as DisplayObject;
                }
            }
            PopUpManager.addPopUp(view, p, flag);
            PopUpManager.centerPopUp(view);
        }
    }

    public static function showAlert(info:String, yesFun:Function = null, noFun:Function = null):void {
        var win:PopUpWindow = new PopUpWindow();
        if (noFun != null)
            win.type = PopUpWindow.AFFIRM;
        else
            win.type = PopUpWindow.PROMPT;
        win.text = info;
        win.addEventListener("onSubmitEvent", function (e:Event) {
            if (yesFun != null)
                yesFun();
        })
        win.addEventListener("onCloseEvent", function (e:Event) {
            if (noFun != null)
                noFun();
        })
        win.show();
    }

    public static function exit():void {
        if (isIOS()) {
            CRMmodel.ane.exit();
        }

        NativeApplication.nativeApplication.exit();
    }

    public static function showToast(text:*):void {
        CRMmodel.ane.showToast(text.toString());
    }

    public static function getIMEI():String {
        return CRMmodel.ane.getIMEI();
    }

    public static function startService():void {
        stopService()
        CRMmodel.ane.startAndriodService();
    }

    public static function stopService():void {
        CRMmodel.ane.stopAndriodService()
    }

    public static function setIsLogin(value:Boolean):void {
        var so:SharedObject = SharedObject.getLocal("jszySharedObject");
        so.data.isLogin = value;
        so.flush();
    }

    public static function getTableField(iid:int, ctable:String, cfield:String, fun:Function):void {
        AccessUtil.remoteCallJava("CommonalityDest", "assemblyQuerySql", function (e:ResultEvent):void {
            var ac:ArrayCollection = e.result as ArrayCollection;
            var result:String = "";
            if (ac.length > 0)
                result = ac[0][cfield];

            fun(result);
        }, "select " + cfield + " from " + ctable + " where iid = " + iid, false);
    }

    public static function formatDateNoHNS(date:Date = null):String {
        if (date == null)
            return "";

        return getFormatDateString("YYYY-MM-DD", date);
    }

    public static function formatDateWithHNS(date:Date = null):String {
        if (date == null)
            return "";

        return getFormatDateString("YYYY-MM-DD HH:NN:SS", date);
    }


    public static function getFormatDateString(fs:String = "YYYY-MM-DD HH:NN:SS", date:Date = null):String {
        if (date == null)
            date = new Date();
        else if (date.fullYear == 1900)
            return "";

        var df:DateFormatter = new DateFormatter();
        df.formatString = fs;
        var s:String = df.format(date);
        s = s.replace(" 24:", " 00:")
        return  s;
    }

    public static function getCurrentVersion():String {
        var appXml:XML = NativeApplication.nativeApplication.applicationDescriptor;
        var ns:Namespace = appXml.namespace();
        return appXml.ns::versionNumber;
    }

    public static function stringToDate(s:String):Date {
        return DateFormatter.parseDateString(s);
    }

    public static function hideBar() {
        CRMmodel.tabbedNavigator.hideTabBar();
    }

    public static function showBar() {
        CRMmodel.tabbedNavigator.showTabBar();
    }

    public static function round(a:Number, n:Number):Number {
        var x = 1;
        for (var i = 1; i <= n; i++)
            x = x * 10;

        return Math.round(a * x) / x;
    }

    public static function trim(s:String):String {
        return StringUtil.trim(s + "");
    }

    public static function calljs(fun:String, param:* = null):* {
        var s;
        try {
            s = ExternalInterface.call(fun, param);
        } catch (e:Error) {
        }

        return s;
    }

    public static function getPersonDepartmentList(iperson:int):ArrayCollection {
        var ac:ArrayCollection = new ArrayCollection();
        for each(var item:Object in CRMmodel.personlist) {
            if (item.iid == iperson)
                ac.addItem({idepartment: item.idepartment});
        }
        for each(var items:Object in CRMmodel.personslist) {
            if (items.iperson == iperson && items.bend != true) {
                ac.addItem({idepartment: items.idepartment});
            }
        }
        return ac;
    }

    public static function labelFunctionFormatDateNoHNS(item:Object, column:GridColumn):String {
        var value = item[column.dataField];
        if (value is Date) {
            return formatDateNoHNS(value);
        } else {
            return value;
        }
    }

    public static function labelFunctionFormatDateOnlyDay(item:Object, column:GridColumn):String {
        var value = item[column.dataField];
        if (value is Date) {
            return getFormatDateString("DD", value);
        } else {
            return value;
        }
    }

    public static function getBoolean(str:*):Boolean {
        var bool:Boolean;
        if (str == null) {
            return false;
        }
        if (str == 1 || str == true || str == "1" || str == "true") {
            bool = true;
        } else {
            bool = false;
        }
        return bool;
    }

    public static function formatFloat(item:Object, column:GridColumn):String {
        var strdate:String;
        if (item[column.dataField] != null && item[column.dataField] != "") {
            strdate = item[column.dataField].toString();
            return roundFloatString(strdate);
        }
        else {
            return null;
        }
    }

    public static function roundFloatString(value:*):String {
        if (CRMtool.isStringNull(value)) {
            return ""
        }

        value = getNumber(value);
        return round(value, 4) + ""
    }

    public static function formatFloatString(value:*):String {
        value = getNumber(value);

        var formater:NumberFormatter = new NumberFormatter();
        formater.precision = 2;
        formater.rounding = NumberBaseRoundType.UP;
        formater.decimalSeparatorFrom = ".";
        formater.decimalSeparatorTo = ".";
        formater.useThousandsSeparator = true;
        return formater.format(value + "");
    }

    public static function cleanSharedObject() {
        var so:SharedObject = SharedObject.getLocal("personcalcSharedObject");
        so.data.code = null;
        so.data.username = null;
        so.flush();

        CRMtool.stopService();
        CRMmodel.ane.iperson = "";
    }

    public static function isListSizeNotZero(list:*):Boolean {
        var ac:ArrayCollection = list as ArrayCollection;
        if (ac != null && ac.length > 0)
            return true;
        else
            return false;
    }

    public static function isIOS():Boolean {
        return CRMmodel.platform.indexOf("android") == -1;
    }

    public static function getSimpleUser():Object {
        return {登录名: CRMmodel.user.登录名,
            设备标识: CRMmodel.user.设备标识,
            用户姓名: CRMmodel.user.用户姓名,
            部门名称: CRMmodel.user.部门名称,
            工作部门: CRMmodel.user.工作部门
        }
    }

}
}
