/**
 * Created by Administrator on 2014/6/20.
 */
package tools {
import mx.rpc.events.FaultEvent;
import mx.rpc.events.ResultEvent;

import spark.components.Alert;

public class ServiceTool {
    public function ServiceTool() {
    }

    public static function login(param, parentView):void {
        param.设备标识 = CRMmodel.设备标识;

        if (CRMtool.isStringNotNull(param.code)) {
            AccessUtil.remoteCallJava("core", "getDepartmentCodeByPersonCode", function (e:ResultEvent):void {
                var user = e.result;
                if (user != null) {
                    CRMtool.showToast("欢迎登录，" + user.职工姓名);
                    CRMmodel.user = user;
                    CRMmodel.ane.iperson = param.code;

                    parentView.loginType = 1;
                    CRMtool.startService();
                } else {
                    CRMtool.showToast("该工号不存在。");
                }

            }, param, true, "验证用户中...", null, function (e:FaultEvent) {
                CRMtool.showToast("网络故障,请检查设置及网络.")
            });


        } else {
            AccessUtil.remoteCallJava("core", "login", function (e:ResultEvent):void {
                var user = e.result;
                if (user != null) {
                    CRMtool.showToast("欢迎登录，" + user.用户姓名);
                    CRMmodel.user = user;
                    CRMmodel.user.password = param.password;
                    CRMmodel.menus = user.menus;
                    CRMmodel.ane.iperson = CRMmodel.user.职工编码 != null ? CRMmodel.user.职工编码 : "0";

                    parentView.loginType = 2;
                    CRMtool.startService()
                } else {
                    CRMtool.showToast("用户名或密码错误。");
                }

            }, param, true, "验证用户中...", null, function (e:FaultEvent) {
                CRMtool.showToast("网络故障,请检查设置及网络.")
            });
        }
    }

    public static function isDateAllow(sDate:String, _fun:Function, isKaoqin:Boolean = false) {
        AccessUtil.remoteCallJava("core", isKaoqin ? "isDateAllowKaoqin" : "isDateAllow", function (e:ResultEvent):void {
            if (e.result as Boolean) {
                _fun.call()
            } else {
                CRMtool.showToast("不允许编辑该日期的数据。");
            }
        }, DateHadle.stringToDate(sDate, "YYYY-MM-DD"), false)
    }
}
}
