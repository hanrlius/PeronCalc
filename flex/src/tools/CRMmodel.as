/**
 * @auth zmm
 * @date 2011-07-28
 * @destination crm单列类，统一控制各种资源
 *
 */
package tools {

import basic.PopUpWindow;

import com.microstarsoft.liurui.ane.ANERoot;

import flash.media.StageWebView;
import flash.sensors.Geolocation;

import mx.collections.ArrayCollection;

import spark.components.TabbedViewNavigator;
import spark.components.View;
import spark.components.ViewNavigator;

[Bindable]
public class CRMmodel {
    private static var instance:CRMmodel;
    public static var user:Object = new Object();
    public static var userId:int = 0;
    public static var ane:ANERoot = new ANERoot();
    public static var tabbedNavigator:TabbedViewNavigator;
    public static var navigator:ViewNavigator;
    public static var platform:String;

    public static var cc:String = '0'; //城郊0,新桥1

    [Bindable]
    public static var loginType:int = 0;//1 职工 2 操作员

    [Bindable]
    public static var welcomeText:String = "";
    //系统部门列表
    public static var departmentlist:ArrayCollection;
    //系统用户列表
    public static var personlist:ArrayCollection;
    public static var personslist:ArrayCollection;

    [Bindable]
    public static var customerArr:ArrayCollection = new ArrayCollection();

    [Bindable]
    public static var selectObjct:Object;

    [Bindable]
    public static var previous:String = "";

    [Bindable]
    public static var customerselectObjct:Object;

    [Bindable]
    public static var jwdObject:String = null;

    //经度
    [Bindable]
    public static var clongitude:String;

    //纬度
    [Bindable]
    public static var clatitude:String;

    //百度地图经度
    [Bindable]
    public static var cblongitude:String;

    //百度地图纬度
    [Bindable]
    public static var cblatitude:String;

    //物理位置
    [Bindable]
    public static var address:String = "";

    //查询出所有客户
    [Bindable]
    public static var allCustomerArr:ArrayCollection;

    public static var swv:StageWebView;

    //获得地理位置
    [Bindable]
    public static var geo:Geolocation;

    [Bindable]
    public static var workdiaryArr:ArrayCollection;

    [Bindable]
    public static var billArr:ArrayCollection;

    public static var selectCustomer:Object;
    public static var menus:ArrayCollection;
    public static var view:View;

    public static var debug:Boolean = false;
    public static var 设备标识:String;
    public static var popUpWindow:PopUpWindow;

    public function CRMmodel() {
        if (instance == null) {
            CRMmodel.instance = this;
        }
    }

    public static function getInstance():CRMmodel {
        if (instance == null) {
            instance = new CRMmodel();
        }

        return instance;
    }
}
}