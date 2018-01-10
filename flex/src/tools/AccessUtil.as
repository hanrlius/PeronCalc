/**
 * Created by aruis on 14-1-22.
 */
package tools {
import basic.PopUpWindow;

import mx.rpc.AbstractOperation;
import mx.rpc.events.FaultEvent;
import mx.rpc.events.ResultEvent;
import mx.rpc.remoting.RemoteObject;

import spark.components.Alert;

public class AccessUtil {

    public function AccessUtil() {
    }

    public static function remoteCallJava(dest:String, method:String, callback:Function = null, param:Object = null,isload:Boolean = false, info:String = "数据处理中...",  p = null, errorCallBack:Function = null):void {
        if (method == null)
            return;


        var ro:RemoteObject = new RemoteObject(dest);
        ro.endpoint = Server.url;
        var aopt:AbstractOperation = ro.getOperation(method);


        if (isload) {//是否显示过渡窗口，
            var win:PopUpWindow = new PopUpWindow();
            win.type = PopUpWindow.TRANSITION;
            win.text = info;
            win.show();
            aopt.addEventListener(ResultEvent.RESULT, function (e:ResultEvent):void {
                win.close();
            });
            aopt.addEventListener(FaultEvent.FAULT, function (e:FaultEvent):void {
                win.close();
            });
            //ro.showBusyCursor = true;
        } else {
            ro.showBusyCursor = false;
        }

        if (callback != null) {//回调函数不为null，添加回调函数

            aopt.addEventListener(ResultEvent.RESULT, callback);

        }
        if (errorCallBack == null) {
            aopt.addEventListener(FaultEvent.FAULT, function (fault:FaultEvent):void {

            }, false, 0, true);
        } else {
            aopt.addEventListener(FaultEvent.FAULT, errorCallBack);
        }
        if (param == null) {
            aopt.send();
        } else {
            aopt.send(param);
        }
        if (ro) {
            ro = null;
            method = null;
            param = null;
            aopt = null;
        }
    }

    private static function aopt_resultHandler(event:ResultEvent):void {

    }


    private function aopt_resultHandler(event:ResultEvent):void {
    }
}
}
