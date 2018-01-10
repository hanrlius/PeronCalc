package com.microstarsoft.bzhs.controller

import com.microstarsoft.bzhs.Core
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * Created by liurui on 15/12/9.
 */
@RestController
public class MessageController {

    @Autowired
    Core core

    @RequestMapping(value = "/message", method = RequestMethod.GET)
    public Score message(@RequestParam String code) {

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

        Score _score = new Score();
        _score.jixiao = jixiao;
        _score.kaoqin = obj.kaoqin;
        _score.anquan = anquan;

        return _score;
    }
}

class Score {
    String jixiao
    String anquan
    String kaoqin
}