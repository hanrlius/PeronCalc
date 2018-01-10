package com.microstarsoft.bzhs.servlets

import com.microstarsoft.bzhs.Core
import org.springframework.beans.factory.annotation.Autowired

import javax.servlet.ServletConfig
import javax.servlet.ServletException
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * Created by Administrator on 2014/6/22.
 */
class MessageServlet extends HttpServlet {

    @Autowired
    private Core core;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        //获得客户端提交用户名密码
        String code = req.getParameter("code");
        DataOutputStream out = new DataOutputStream(resp.getOutputStream())
        def map = [:]
        map.code = code;
        map.date = new Date().format("yyyy-MM-dd")
        Map obj = core.getPersonCalcToday(map)
        def jixiao = obj.jixiao;
        def anquan = obj.anquan;
        if (obj.jixiaoread)
            jixiao = 0
        if (obj.anquanread)
            anquan = 0

        String result = jixiao + "#" + anquan + "#" + obj.kaoqin;
        println result
        out.writeUTF(result);
        out.close()
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//        super.doGet(req, resp)
        resp.getOutputStream().println(core == null ? 'null' : 'notNull')
    }
}
