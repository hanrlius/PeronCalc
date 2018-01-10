package com.microstarsoft.bzhs

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.jar.JarFile

/**
 * Created by Administrator on 2014/6/20.
 */
class Tool {

    public static initYml() {
        //自动创建yml配置文件
        File ymlfile = new File(getProjectPath() + '/application.yml');

        //考虑到jar内获取文件,所以同过getResourceAsStream获取文件路径
        if (!ymlfile.exists()) {
            ymlfile.write(this.getClass().getResourceAsStream("/application.yml").text);
        }
    }

    public static initWebStatic() {
        //自动创建static 静态文件
        File webapp = new File(getProjectPath() + '/static/')

        if (!webapp.exists()) {
            // TODO 复制public 文件夹内容 至相应路径

            webapp.mkdir()

            if (isInJar()) {
                JarFile file = new JarFile(getProjectPath() + '/server-1.0.jar');
                file.entries().each { f ->
                    if (f.name.indexOf('static/') == 0) {
                        if (f.name.length() > 'static/'.length()) {
                            FileOutputStream fileOutputStream = new FileOutputStream(new File(getProjectPath() + '/' + f.name))
                            copyStream(file.getInputStream(f), fileOutputStream);
                            fileOutputStream.close()
                        }
                    }
                }
            } else {

            }


        }
    }

    public static void copyStream(InputStream input, OutputStream output)
            throws IOException {
        byte[] buffer = new byte[1024]; // Adjust if you want
        int bytesRead;
        while ((bytesRead = input.read(buffer)) != -1) {
            output.write(buffer, 0, bytesRead);
        }
    }

    public static boolean isInJar() {
        return this.getClass().getResource("/static/").toURI().toString().indexOf('jar:') == 0
    }

    public static String createDbPassword(String unEncryptPassword) {
        String strKey = "H5nxTMN126uvwyzSpq3YP9wsklmZUroCg740VtDEdehigBJfK8abWRQAFGcLXI";
        int key = 6;
        return encryptString(unEncryptPassword, strKey, key);
    }

    public static String encryptString(String strSource, String strKey, int key) {
        ArrayList arrayList = strKey.toList();

        String text = "";
        ArrayList arrayList2 = new ArrayList();
        for (int j = 0; j < arrayList.size() - 1; j++) {
            if (arrayList[j] == arrayList[j + 1] && !arrayList2.contains(arrayList[j])) {
                arrayList2.add(arrayList[j]);
                if (text == "") {
                    text = arrayList[j];
                } else {
                    text = text + "," + arrayList[j];
                }
            }
        }
        if (arrayList2.size() > 0) {
            println("密钥字符串设置错误！[" + text + "]重复，导致无法逆向解密");
        }
        String text2 = "";
        int num = 0;
        for (int k = 0; k < strSource.size(); k++) {
            key = key + k + num;
            if (key > 10000) {
                num = 1;
                key = 1;
            }
            int num2 = strKey.indexOf(strSource[k]);
            if (num2 >= 0) {
                for (int l = 0; l < key; l++) {
                    num2++;
                    if (num2 == strKey.size()) {
                        num2 = 0;
                        num++;
                    }
                }
                text2 += strKey[num2];
            } else {
                text2 += strSource[k];
            }
        }
        return text2;
    }

    public static def round(a, n) {
        def x = 1;
        for (def i = 1; i <= n; i++)
            x = x * 10;

        return Math.round(a * x) / x;
    }

    public static Date stringToDate(String dateStr, String formatStr) {
        DateFormat dd = new SimpleDateFormat(formatStr);
        Date date = null;
        try {
            date = dd.parse(dateStr);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return date;
    }

    private static void bindPort(String host, int port) throws Exception {
        Socket s = new Socket();
        s.bind(new InetSocketAddress(host, port));
        s.close();
    }

    public static boolean isPortAvailable(int port) {
        try {
//            bindPort("0.0.0.0", port);
            bindPort(InetAddress.getLocalHost().getHostAddress(), port);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    //获取当前文件的运行路径
    public static String getProjectPath() {


        java.net.URL url = com.microstarsoft.bzhs.Application.class.getProtectionDomain().getCodeSource().getLocation();

        String filePath = null;

        try {
            filePath = java.net.URLDecoder.decode(url.getPath(), "utf-8").replace("file:", "").replace("jar:", "");
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (filePath.lastIndexOf('/') == filePath.length() - 1) {
            filePath = filePath.substring(0, filePath.length() - 1)
        }

        if (filePath.endsWith(".jar") || filePath.endsWith(".jar!")) {
            filePath = filePath.substring(0, filePath.lastIndexOf("/") + 1);
        }

        java.io.File file = new java.io.File(filePath);

        filePath = file.getAbsolutePath();

        return filePath;

    }

}
