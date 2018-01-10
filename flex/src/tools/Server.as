/**
 * Created by aruis on 14-1-22.
 */
package tools {
public class Server {

    public function Server() {
    }

    public static function get url():String {
        return basicUrl + "/messagebroker/amf"
    }

    public static function get basicUrl():String {
        return "http://" + ip + ":" + port + ( CRMtool.isStringNotNull(contextRoot) ? "/" + contextRoot : "");
    }

    public static function get ip():String {
        //return so.data.ip;
        return CRMmodel.ane.ip;
    }

    public static function set ip(value:String):void {
        //so.data.ip = value;
        //so.flush();
        CRMmodel.ane.ip = value;
    }

    public static function get port():String {
        return CRMmodel.ane.port;
    }

    public static function set port(value:String):void {
        CRMmodel.ane.port = value;
    }

    public static function get contextRoot():String {
        return CRMmodel.ane.root;
    }

    public static function set contextRoot(value:String):void {
        CRMmodel.ane.root = value;
    }
}
}
