package com.microstarsoft.bzhs

import groovy.sql.Sql
import org.joda.time.Days
import org.joda.time.LocalDate
import org.joda.time.Minutes
import org.springframework.beans.factory.annotation.Value
import org.springframework.flex.remoting.RemotingDestination
import org.springframework.flex.remoting.RemotingInclude
import org.springframework.stereotype.Service

/**
 * 用于桥接flex到cm服务
 * Created by liurui on 14/10/24.
 */
@Service('core')
@RemotingDestination(channels = "my-amf")
class Core {

    private static Sql _db;

    @Value('${url}')
    String url;

    @Value('${dbname}')
    String dbname;

    @Value('${dbusername}')
    String username;

    @Value('${dbpassword}')
    String password;

    @Value('${driverClassName}')
    String driverClassName;


    @Value('${jixiao}')
    String jixiao_id;

    @Value('${anquan}')
    String anquan_id;

    @Value('${jixiao_da}')
    String jixiao_da_id;

    @Value('${leibie_jixiao}')
    String leibie_jixiao;

    @Value('${leibie_anquan}')
    String leibie_anquan;


    @RemotingInclude
    public String sayHello() {
        return "hello world"
    }

    Sql getDb() {
        if (_db == null) {
            _db = Sql.newInstance(url + dbname, username, password, driverClassName);
        }
        return _db;
    }

    @RemotingInclude
    String getVersion() {
        def row = db.firstRow("SELECT 参数值 FROM config WHERE 参数='BZHSA手机端版本号'")
        return row.参数值;
    }

    @RemotingInclude
    List 职工台账查询(def map) {
        return db.rows("select 职工.职工编码 ,职工.职工姓名 ,a.得分 as 绩效,b.得分 as 安全,a.日期,c.考勤名称 as 考勤 from 职工 " +
                "  left join bzhsa核算明细_个人得分 a on 职工.职工编码 = a.职工编码 and a.核算项目ID='$jixiao_id' " +
                "  left join bzhsa核算明细_个人得分 b on 职工.职工编码 = b.职工编码 and b.核算项目ID='$anquan_id' " +
                "  left join dbo.bzhsa考勤明细 c on c.职工编码 = 职工.职工编码 and a.日期 = c.日期" +
                "  where 职工.职工编码='$map.code' and a.日期=b.日期 and a.日期>='$map.begin' and a.日期<='$map.end'" +
                " order by a.日期");
    }

    @RemotingInclude
    List 班组台账查询(def map) {
        return db.rows("select 部门.部门编码 ,部门.部门名称 ,a.得分 as 绩效,b.得分 as 安全,a.日期 from 部门 " +
                "  left join bzhsa核算明细_班组得分 a on 部门.部门编码 = a.部门编码 and a.核算项目ID='$jixiao_id' " +
                "  left join bzhsa核算明细_班组得分 b on 部门.部门编码 = b.部门编码 and b.核算项目ID='$anquan_id' " +
                "  where 部门.部门编码='$map.code' and a.日期=b.日期  and a.日期>='$map.begin' and a.日期<='$map.end'" +
                " order by a.日期");
    }

    @RemotingInclude
    Map login(def map) {
        def row
        map.username = map.username.toUpperCase()

        println(map.username)
        def selectUserSql = "select users.*,职工.职工编码,职工.职工姓名,职工.工作部门,部门.部门名称 from users " +
                "  left join 职工 on 职工.职工编码 = users.职工  " +
                "  left join 部门 on 部门.部门编码 = 职工.工作部门  " +
                "where upper(登录名)='$map.username'"

        println(selectUserSql)

        if (map.password == null) {
            row = db.firstRow(selectUserSql);
        } else {
            def pw = Tool.createDbPassword(map.password);
            row = db.firstRow(selectUserSql + " and users.密码='$pw'");
        }


        if (row != null) {
            def list = db.rows(""" select 模块编码,执行,查看,新建,删除,修改,保存 from userright where  查看=1 and  upper(登录名)=?
 union
 select 模块编码,执行,查看,新建,删除,修改,保存 from roleright where 查看=1 and  角色编码 in (select 角色编码 from  userrole where upper(登录名)=?)
union
select 菜单编码 AS 模块编码,执行,查看,新建,删除,修改,保存 from MenuRights where 查看=1 and  (upper(用户名)=? or MenuRights.角色名 IN (select 角色编码 from  userrole where upper(登录名)=?))
""", [map.username, map.username, map.username, map.username]);
            row.menus = list;
        }

        LogVO logVO = new LogVO()
        logVO.事件类型 = "登录"
        logVO.类别 = "登录"
        logVO.操作人登录名 = map.username
        if (row != null) {
            if (row.职工姓名 != null)
                logVO.操作人姓名 = row.职工姓名

            if (row.工作部门 != null) {
                logVO.操作部门名称 = row.部门名称
                logVO.操作部门编码 = row.工作部门
            }
        }
        logVO.设备标识 = map.设备标识

        insertLog(logVO)

        return row
    }

    @RemotingInclude
    void logout(def map) {
        LogVO logVO = new LogVO(map)
        logVO.事件类型 = "注销"
        logVO.类别 = "注销"

        insertLog(logVO)
    }

    @RemotingInclude
    Map getDepartmentCodeByPersonCode(def map) {
        return db.firstRow("select 职工姓名,工作部门,职工编码,部门.部门名称,职工.工作部门 as 部门编码 from 职工" +
                "  left join 部门 on 部门.部门编码 = 职工.工作部门" +
                " where 职工编码='$map.code'");
    }

    @RemotingInclude
    def insertRizi(def map) {
        db.withTransaction {
            int id = db.firstRow("SELECT max(id) AS id FROM 职工日资").id;
            id++;
            def i = 1;
            db.eachRow("select 职工编码 from 职工 where 工作部门='$map.code'", {
                db.execute("insert into 职工日资 (id,日期,部门编码,职工编码,工资类别,台账顺序)" +
                        "values( '$id','$map.date','$map.code','$it.职工编码','01','$i' )")
                i++;
                id++;
            })
        }
    }

    @RemotingInclude
    List get考勤类别(def map) {
        String duicode = getDuiCode(map)
        return db.rows("SELECT a.* FROM bzhsa考勤类别 a \n" +
                "LEFT JOIN BZHSA部门考勤类别 b ON a.考勤类别ID = b.考勤类别ID AND isnull(b.禁用,0)=0\n" +
                "WHERE b.部门编码='$duicode' AND isnull(a.是否禁用,0)=0 ORDER BY a.序号")
    }

    @RemotingInclude
    List getDepartment(def map) {
        return db.rows("select 部门.部门编码,部门.部门名称 from 用户部门\n" +
                "  left join 部门 on 部门.部门编码 = 用户部门.部门编码\n" +
                "where isnull(部门.禁用,0)!=1 and  用户编码='$map.code'")
    }

    @RemotingInclude
    List getDepartmentGeren(def map) {
        def r = db.firstRow("select 登录名 from users where 登录名='" + map.code + "_个人得分'")
        def s = "select 部门.部门编码,部门.部门名称,isnull(部门.是否区队,0) 是否区队 from 用户部门 " +
                "  left join 部门 on 部门.部门编码 = 用户部门.部门编码 " +
                "where isnull(部门.禁用,0)!=1 and  用户编码='" + map.code + "${r == null ? '' : '_个人得分'}'"
        println(s)
        return db.rows(s)

    }

    @RemotingInclude
    List getParentDepartment(def map) {
        return db.rows("select 部门.部门编码,部门.部门名称 from 用户部门\n" +
                "  left join 部门 on 部门.部门编码 = 用户部门.部门编码\n" +
                "where isnull(部门.禁用,0)!=1 and 用户编码='$map.code'")
    }

    @RemotingInclude
    List getParentDepartmentBanzu(def map) {
        def r = db.firstRow("select 登录名 from users where 登录名='" + map.登录名 + "_个人得分'")

        def user = map.登录名;

        if (r != null) {
            user += "_个人得分";
        }

        List 部门List = db.rows("SELECT  b.部门编码 ,a.部门名称 FROM 用户部门 b, 部门 a \n" +
                "WHERE a.部门编码=b.部门编码 AND b.用户编码='$user' AND isnull(a.末级,0)=0\n" +
                "AND isnull(a.禁用,0)!=1\n" +
                "ORDER BY a.部门编码")

        if (部门List.size() == 0) { //当前登录的人是末级部门,则想办法取出其上级部门列表
            return db.rows("select a.部门编码 ,a.部门名称 from 部门 a\n" +
                    "LEFT JOIN 部门 b on b.上级 = a.部门编码\n" +
                    "LEFT JOIN 用户部门 c on c.部门编码 = b.部门编码\n" +
                    "WHERE isnull(a.禁用,0)!=1 and  c.用户编码 = '$user'")
        } else {
            return 部门List;
        }
    }

    @RemotingInclude
    List getDepartmentChildren(def map) {

        def returnRow;
        db.withTransaction {
            def departments = db.rows("""
    select 部门编码,部门名称 from 部门
  where isnull(部门.禁用,0)!=1 and 部门.部门编码 like '${map.code}%' and 部门.部门编码 != '${map.code}'
        and 部门.末级 = 1 and isnull(部门.是否虚拟,0)=0
        and 部门.部门编码 in (select 部门.部门编码 from 用户部门
    left join 部门 on 部门.部门编码 = 用户部门.部门编码
  where isnull(部门.禁用,0)!=1 and 用户编码='${map.username}')
""".toString())

            departments.each { dep ->
                def isIn = db.firstRow("select 部门编码 from bzhsa核算明细_班组得分 where 部门编码  = '$dep.部门编码'  and 日期='$map.date'")
                if (!isIn) {
                    def yearAndQi = getYearAndQi(map.date)


                    def item = [:]
                    item.模块类别 = '每日得分';
                    item.核算类别ID = leibie_jixiao
                    item.核算类别名称 = '绩效工资'
                    item.核算项目ID = jixiao_id
                    item.核算项目编码 = '01001'
                    item.核算项目名称 = '绩效日常得分';
                    item.年 = yearAndQi.年
                    item.期 = yearAndQi.期

                    db.execute("""
insert into bzhsa核算明细_班组得分
                       (明细ID,日期,部门编码,部门名称,模块类别 ,核算类别ID ,核算类别名称 ,核算项目ID ,核算项目编码 ,核算项目名称,年,期 )
                        values(newid(),'$map.date','$dep.部门编码','$dep.部门名称','$item.模块类别','$item.核算类别ID','$item.核算类别名称','$item.核算项目ID','$item.核算项目编码','$item.核算项目名称','$item.年','$item.期')
""".toString())

                    item.核算类别ID = leibie_anquan
                    item.核算类别名称 = '安全结构工资'
                    item.核算项目ID = anquan_id
                    item.核算项目编码 = '02001'
                    item.核算项目名称 = '安全日常得分'

                    db.execute("""
insert into bzhsa核算明细_班组得分
                       (明细ID,日期,部门编码,部门名称,模块类别 ,核算类别ID ,核算类别名称 ,核算项目ID ,核算项目编码 ,核算项目名称,年,期 )
                        values(newid(),'$map.date','$dep.部门编码','$dep.部门名称','$item.模块类别','$item.核算类别ID','$item.核算类别名称','$item.核算项目ID','$item.核算项目编码','$item.核算项目名称','$item.年','$item.期')
""".toString())

                }
            }


            def _db = "select z.部门编码 ,z.部门名称 as 班组, a.得分 as 绩效,b.得分 as 安全 from 部门 z\n" +
                    "  left join dbo.bzhsa核算明细_班组得分 a on a.部门编码 = z.部门编码 and a.核算项目ID='$jixiao_id' and a.日期='$map.date'\n" +
                    "  left join dbo.bzhsa核算明细_班组得分 b on b.部门编码 = z.部门编码 and b.核算项目ID='$anquan_id' and b.日期='$map.date'\n" +
                    "   where   z.部门编码 in (\n  " +
                    //" select 部门编码 from 部门 where isnull(部门.禁用,0)!=1 and 部门.部门编码 like '" + map.code + "%' and 部门.部门编码 != '" + map.code + "' union " + //正式上线后这一行 应该注释掉
                    " select 部门编码 from bzhsa核算明细_班组得分 where 部门编码 like '" + map.code + "%' and 部门编码 != '" + map.code + "' and 日期='$map.date'\n" +
                    ")\n" +
                    " and z.部门编码 in (select 部门.部门编码 from 用户部门 \n" +
                    "  left join 部门 on 部门.部门编码 = 用户部门.部门编码 \n" +
                    " where isnull(部门.禁用,0)!=1 and 用户编码='$map.username') \n" +
                    " order by z.部门编码"


            returnRow = db.rows(_db.toString())
        }
        return returnRow
    }

    @RemotingInclude
    Boolean savePersonsCalc(def map) {
        try {
            List<Map> list = map.list;
            def date = map.date;
            def user = map.user;
            db.withTransaction {
                list.each {
                    def _d = it.日期.format("yyyy-MM-dd")
                    def _z = it.摘要 ? it.摘要 : '';
                    db.execute("update 职工日资 set 收入=$it.收入,摘要='$_z',操作员='$user',时间='$date' where 日期='$_d' and 部门编码='$it.部门编码' and 台账顺序='$it.台账顺序'")
                }
            }

        } catch (Exception e) {
            e.printStackTrace()
            return false;
        }
        return true;
    }

    @RemotingInclude
    public Map getPersonCalcToday(def map) {
        def _sql = "select isnull(a.得分,0) 绩效,isnull(b.得分,0) 安全,a.部门名称,isnull(a.已阅,0) as 绩效已阅,isnull(b.已阅,0) as 安全已阅 from 职工 z\n" +
                "  left join dbo.BZHSA核算明细_个人得分 a on a.职工编码 = z.职工编码 and a.核算项目ID='$jixiao_id' and a.日期='$map.date'\n" +
                "  left join dbo.BZHSA核算明细_个人得分 b on b.职工编码 = z.职工编码 and b.核算项目ID='$anquan_id' and b.日期='$map.date'\n" +
                "   where a.部门编码 = b.部门编码 and   z.职工编码 in (\n  " +
                " select 职工编码 from BZHSA核算明细_个人得分 where 职工编码='$map.code' and 日期='$map.date'\n" +
                ")\n" +
                " order by z.工作部门,z.台账顺序,z.职工编码"

        def row1 = db.firstRow(_sql)

        def row2 = db.firstRow("select 考勤名称 from bzhsa考勤明细 where 职工编码='$map.code' and 日期='$map.date'  and isnull(已阅,0)=0")

        def jixiao = 0, anquan = 0, kaoqin = "0", bumen = 0, jixiaoread = false, anquanread = false;

        if (row1 != null) {
            db.execute("update bzhsa核算明细_个人得分 set 已阅='1' where 职工编码='" + map.code + "' and 日期='" + map.date + "' and 模块类别='每日得分'" +
                    " and (核算项目ID='$jixiao_id' or 核算项目ID='$anquan_id')")

            jixiao = Tool.round(row1.绩效, 2)
            anquan = Tool.round(row1.安全, 2)
            bumen = row1.部门名称
            jixiaoread = row1.绩效已阅
            anquanread = row1.安全已阅
        }

        if (row2 != null) {
            kaoqin = row2.考勤名称;
            db.execute("update bzhsa考勤明细 set 已阅='1' where 职工编码='" + map.code + "' and 日期='" + map.date + "' ")
        }

        def rmap = [jixiao: jixiao, anquan: anquan, kaoqin: kaoqin, bumen: bumen, jixiaoread: jixiaoread, anquanread: anquanread]

        return rmap
    }

    @RemotingInclude
    Boolean save考勤(Map obj) {
        try {
            def yearAndQi = getYearAndQi(obj.日期)
            obj.年 = yearAndQi.年
            obj.期 = yearAndQi.期

            def user = obj.user;
            obj.remove("user");
            def id = java.util.UUID.randomUUID();
            obj.id = id

            db.withTransaction {
                db.execute("delete bzhsa考勤明细 where 日期='$obj.日期' and 职工编码='$obj.职工编码' ")
                db.execute("insert into bzhsa考勤明细 (考勤ID,序号,日期,部门编码,部门名称,职工编码,职工姓名,职务岗位,考勤类别ID,考勤名称,考勤符号,出勤数,出勤值,录入人,录入人姓名,录入时间,年,期,备注)" +
                        "values ('$obj.id','$obj.序号','$obj.日期','$obj.部门编码','$obj.部门名称','$obj.职工编码','$obj.职工姓名','$obj.职务岗位','$obj.考勤类别ID','$obj.考勤名称','$obj.考勤符号',$obj.出勤数,$obj.出勤值,'$obj.录入人','$obj.录入人姓名',getDate(),'$obj.年','$obj.期','手机录入')")

            }

            LogVO logVO = new LogVO(user)
            logVO.事件类型 = "数据修改"
            logVO.类别 = "修改"
            logVO.表名 = "BZHSA考勤明细"
            logVO.详细信息 = obj.toString()
            logVO.ID字段名 = "考勤ID"
            logVO.ID值 = id

            insertLog(logVO)

            return true
        } catch (Exception e) {
            e.printStackTrace()
            return false
        }
    }

    @RemotingInclude
    List getDepartmentPersons(def map) {
        def row = db.rows("select 职工编码 as 工号,职工姓名 as 姓名,isnull(台账顺序,0) 台账顺序 from 职工 where 工作部门='$map.code' order by 台账顺序,职工编码")
        return row
    }

    @RemotingInclude
    List getDepartmentKaoqinPersons(def map) {
        def row = db.rows("select 职工.职工编码 as 工号,职工.职工姓名 as 姓名,isnull(台账顺序,0) 台账顺序,a.考勤名称 as 考勤情况,职工.工作部门,部门.部门名称,职工.职务岗位,职工.拼音 as pinyin from 职工 " +
                " left join bzhsa考勤明细 a on 职工.职工编码 = a.职工编码 and a.日期='$map.date' " +
                " left join 部门 on 部门.部门编码 = 职工.工作部门 " +
                " where 职工.职工编码 in (\n" +
                "  select 职工编码 from 职工 where 工作部门 like '$map.code%' and 职工编码 not in (select 职工编码 from bzhsa考勤明细 where 日期='$map.date') union  \n" +
                "  select 职工编码 from bzhsa考勤明细 where 部门编码 like '$map.code%' and 日期='$map.date'\n" +
                "  ) order by 职工.工作部门,台账顺序,职工.职工编码")
        return row
    }

    @RemotingInclude
    List getDepartmentDafenPersons(def map) {
        def returnRow;

        db.withTransaction {
            //def fr = db.firstRow("select 职工编码 from BZHSA核算明细_个人得分 where 部门编码='$map.code' and 日期='$map.date'")
            def yearAndQi = getYearAndQi(map.date)

            if (true) {
                def item = [:]
                item.模块类别 = '每日得分';
                item.核算类别ID = leibie_jixiao
                item.核算类别名称 = '绩效工资'
                item.核算项目ID = jixiao_id
                item.核算项目编码 = '01001'
                item.核算项目名称 = '绩效日常得分'
                item.年 = yearAndQi.年
                item.期 = yearAndQi.期

                db.execute("insert into bzhsa核算明细_个人得分 \n" +
                        "(明细ID,序号,日期,部门编码,部门名称,职工编码,职工姓名,模块类别 ,核算类别ID ,核算类别名称 ,核算项目ID ,核算项目编码 ,核算项目名称,年,期 )" +
                        " select newid(),isnull(台账顺序,0),'$map.date',工作部门,部门.部门名称,职工编码,职工姓名,'$item.模块类别','$item.核算类别ID','$item.核算类别名称','$item.核算项目ID','$item.核算项目编码','$item.核算项目名称','$item.年','$item.期' from 职工\n" +
                        "  left join  部门 on 职工.工作部门 = 部门.部门编码 where 职工.工作部门='$map.code'" +
                        " and 职工编码 not in (select 职工编码 from bzhsa核算明细_个人得分 where 日期='$map.date' and 核算项目ID='$item.核算项目ID'  ) ")

                item.模块类别 = '每日得分';
                item.核算类别ID = leibie_anquan
                item.核算类别名称 = '安全结构工资'
                item.核算项目ID = anquan_id
                item.核算项目编码 = '02001'
                item.核算项目名称 = '安全日常得分'

                db.execute("insert into bzhsa核算明细_个人得分 \n" +
                        "(明细ID,序号,日期,部门编码,部门名称,职工编码,职工姓名,模块类别 ,核算类别ID ,核算类别名称 ,核算项目ID ,核算项目编码 ,核算项目名称,年,期 )" +
                        " select newid(),isnull(台账顺序,0),'$map.date',工作部门,部门.部门名称,职工编码,职工姓名,'$item.模块类别','$item.核算类别ID','$item.核算类别名称','$item.核算项目ID','$item.核算项目编码','$item.核算项目名称','$item.年','$item.期' from 职工\n" +
                        "  left join  部门 on 职工.工作部门 = 部门.部门编码 where 职工.工作部门='$map.code'" +
                        " and 职工编码 not in (select 职工编码 from bzhsa核算明细_个人得分 where 日期='$map.date' and 核算项目ID='$item.核算项目ID'  ) ")


            }


            def _db = "select z.职工编码 as 工号,z.职工姓名 as 姓名,isnull(台账顺序,0) 台账顺序, a.得分 as 绩效,b.得分 as 安全,z.拼音 as pinyin from 职工 z\n" +
                    "  left join dbo.BZHSA核算明细_个人得分 a on a.职工编码 = z.职工编码 and a.核算项目ID='$jixiao_id' and a.日期='$map.date'\n" +
                    "  left join dbo.BZHSA核算明细_个人得分 b on b.职工编码 = z.职工编码 and b.核算项目ID='$anquan_id' and b.日期='$map.date'\n" +
                    "   where a.部门编码 = b.部门编码 and   z.职工编码 in (\n  " +
                    " select 职工编码 from 职工 where 工作部门='$map.code' union " +
                    " select 职工编码 from BZHSA核算明细_个人得分 where 部门编码='$map.code' and 日期='$map.date'\n" +
                    ")\n" +
                    "order by z.工作部门,z.台账顺序,z.职工编码"

            println(_db)

            returnRow = db.rows(_db)
        }
        return returnRow
    }

    @RemotingInclude
    Map save个人得分(Map obj) {

        def result = [:]

        try {
            def dafen = getPersonCalcToday([code: obj.职工编码, date: obj.日期])
            def user = obj.user;
            obj.remove("user");

            if ((dafen.jixiao > 0 || dafen.anquan > 0) && dafen.bumen != obj.部门名称) {
                result.flag = false;
                result.str = "已在" + dafen.bumen + "部门打分，请检查。"
                return result;
            }

            def jixiao = obj

            def yearAndQi = getYearAndQi(obj.日期)
            jixiao.年 = yearAndQi.年
            jixiao.期 = yearAndQi.期

            jixiao.模块类别 = '每日得分';
            jixiao.核算类别ID = leibie_jixiao
            jixiao.核算类别名称 = '绩效工资'
            jixiao.核算项目ID = jixiao_id
            jixiao.核算项目编码 = '01001'
            jixiao.核算项目名称 = '绩效日常得分'
            def id = java.util.UUID.randomUUID();
            jixiao.id = id;

            _save个人得分(jixiao)

            LogVO logVO = new LogVO(user)
            logVO.事件类型 = "数据修改"
            logVO.类别 = "修改"
            logVO.模块 = "个人核算明细"
            logVO.表名 = "bzhsa核算明细_个人得分"
            logVO.ID字段名 = "明细ID"
            logVO.ID值 = id

            logVO.详细信息 = jixiao.toString()
            insertLog(logVO)


            def anquan = jixiao.clone()

            anquan.得分 = anquan.安全得分
            anquan.模块类别 = '每日得分';
            anquan.核算类别ID = leibie_anquan
            anquan.核算类别名称 = '安全结构工资'
            anquan.核算项目ID = anquan_id
            anquan.核算项目编码 = '02001'
            anquan.核算项目名称 = '安全日常得分'

            id = java.util.UUID.randomUUID();
            anquan.id = id;

            _save个人得分(anquan)
            logVO.ID值 = id
            logVO.详细信息 = anquan.toString()
            insertLog(logVO)

            result.flag = true
        } catch (Exception e) {
            e.printStackTrace()
            result.flag = false
        }

        return result;
    }

    @RemotingInclude
    private _save个人得分(item) {
        db.withTransaction {
            db.execute("delete bzhsa核算明细_个人得分 where 日期='$item.日期' and 职工编码='$item.职工编码' and 核算项目ID='$item.核算项目ID'")
            db.execute("insert into bzhsa核算明细_个人得分 (明细ID,序号,日期,部门编码,部门名称,职工编码,职工姓名,录入人,录入人姓名,录入时间,得分,模块类别 ,核算类别ID ,核算类别名称 ,核算项目ID ,核算项目编码 ,核算项目名称,年,期,来源 )" +
                    "values ('$item.id','$item.序号','$item.日期','$item.部门编码','$item.部门名称','$item.职工编码','$item.职工姓名','$item.录入人','$item.录入人姓名',getDate(),$item.得分,'$item.模块类别','$item.核算类别ID','$item.核算类别名称','$item.核算项目ID','$item.核算项目编码','$item.核算项目名称','$item.年','$item.期','手机录入')")

        }
    }

    @RemotingInclude
    private Map getYearAndQi(date) {
        return db.firstRow("select 年,期 from 公共期间 where 类别=4 and  '$date' between 开始日期 and 结束日期 ")
    }

    @RemotingInclude
    Boolean save班组得分(Map obj) {

        try {
            def jixiao = obj
            def user = obj.user;
            obj.remove("user");

            def yearAndQi = getYearAndQi(obj.日期)
            jixiao.年 = yearAndQi.年
            jixiao.期 = yearAndQi.期

            jixiao.模块类别 = '每日得分';
            jixiao.核算类别ID = leibie_jixiao
            jixiao.核算类别名称 = '绩效工资'
            jixiao.核算项目ID = jixiao_id
            jixiao.核算项目编码 = '01001'
            jixiao.核算项目名称 = '绩效日常得分'
            def id = java.util.UUID.randomUUID();
            jixiao.id = id;

            _save班组得分(jixiao)

            LogVO logVO = new LogVO(user)
            logVO.事件类型 = "数据修改"
            logVO.类别 = "修改"
            logVO.模块 = "班组核算明细"
            logVO.表名 = "bzhsa核算明细_班组得分"
            logVO.ID字段名 = "明细ID"
            logVO.ID值 = id

            logVO.详细信息 = jixiao.toString()
            insertLog(logVO)

            def anquan = jixiao.clone()

            anquan.得分 = anquan.安全得分
            anquan.模块类别 = '每日得分';
            anquan.核算类别ID = leibie_anquan
            anquan.核算类别名称 = '安全结构工资'
            anquan.核算项目ID = anquan_id
            anquan.核算项目编码 = '02001'
            anquan.核算项目名称 = '安全日常得分'

            id = java.util.UUID.randomUUID();
            anquan.id = id;
            _save班组得分(anquan)

            logVO.ID值 = id
            logVO.详细信息 = anquan.toString()
            insertLog(logVO)

            return true
        } catch (Exception e) {
            e.printStackTrace()
            return false
        }

    }

    @RemotingInclude
    private _save班组得分(item) {
        db.withTransaction {
            db.execute("delete bzhsa核算明细_班组得分 where 日期='$item.日期' and 部门编码='$item.部门编码' and 核算项目ID='$item.核算项目ID'")
            db.execute("insert into bzhsa核算明细_班组得分 (明细ID,日期,部门编码,部门名称,录入人,录入人姓名,录入时间,得分,模块类别 ,核算类别ID ,核算类别名称 ,核算项目ID ,核算项目编码 ,核算项目名称 ,年,期,来源)" +
                    "values ('$item.id','$item.日期','$item.部门编码','$item.部门名称','$item.录入人','$item.录入人姓名',getDate(),$item.得分,'$item.模块类别','$item.核算类别ID','$item.核算类别名称','$item.核算项目ID','$item.核算项目编码','$item.核算项目名称','$item.年','$item.期','手机录入')")

        }
    }

    @RemotingInclude
    public List querySql(String _db) {
        return db.rows(_db)
    }

    @RemotingInclude
    public String getDuiCode(def map) {
        def row = db.firstRow("select 部门编码 from 部门 where 部门名称=(select 队 from 部门 where 部门编码='$map.code')")
        if (row == null) {
            return ""
        } else {
            return row.部门编码
        }
    }

    @RemotingInclude
    public Boolean removeGerenItem(def map) {
        try {
            def user = map.user;
            map.remove("user");

            db.execute("delete bzhsa核算明细_个人得分 where 日期='$map.日期' and 部门编码='$map.部门编码' and 职工编码='$map.职工编码' ")

            LogVO logVO = new LogVO(user)
            logVO.事件类型 = "数据删除"
            logVO.类别 = "删除"
            logVO.模块 = "个人核算明细"
            logVO.表名 = "bzhsa核算明细_个人得分"
            logVO.详细信息 = map.toString()

            insertLog(logVO)
            return true
        } catch (Exception e) {
            e.printStackTrace()
            return false
        }
    }

    @RemotingInclude
    public String cheackPW(def map) {
        try {
            def re = login(map)
            if (re == null) {
                return "旧密码验证不正确。"
            } else {
                def pw = Tool.createDbPassword(map.newPassword);
                db.execute("update users set 密码='$pw' where 登录名=$map.username");
                return "设置成功。"
            }
        } catch (Exception e) {
            e.printStackTrace()
            return "设置失败。"
        }
    }

    @RemotingInclude
    List queryKaoqin(def map) {
        def row = db.rows("select a.职工编码 as 工号,a.职工姓名 as 姓名,a.考勤名称 as 考勤,a.出勤值 as 考勤值 from  bzhsa考勤明细 a\n" +
                "left join 职工 z on z.职工编码 = a.职工编码\n" +
                "where  a.日期='$map.date' and a.部门编码='$map.code'\n" +
                "order by z.台账顺序,z.职工编码")
        return row
    }

    @RemotingInclude
    List queryKaoqinTaizhang(def map) {
        def row = db.rows("select 职工编码 as 工号,日期, 职工姓名 as 姓名,考勤名称 as 考勤,出勤值 as 考勤值 from  bzhsa考勤明细 where  日期>='$map.begin' and 日期<='$map.end' and 职工编码='$map.code'" +
                " order by 日期")
        return row
    }

    @RemotingInclude
    List queryJixiao(def map) {
        def _s = "select z.职工编码 as 工号,z.职工姓名 as 姓名, a.得分 as 绩效,b.得分 as 绩效大分 from 职工 z\n" +
                "  left join dbo.BZHSA核算明细_个人得分 a on a.职工编码 = z.职工编码 and a.核算项目ID='$jixiao_id' and a.日期='$map.date'\n" +
                "  left join dbo.BZHSA核算明细_个人得分 b on b.职工编码 = z.职工编码 and b.核算项目ID='$jixiao_da_id' and b.日期='$map.date'\n" +
                "   where   z.职工编码 in (\n" +
                " select 职工编码 from BZHSA核算明细_个人得分 where 职工编码 = '$map.usercode' or ( 部门编码='$map.code' and 日期='$map.date')\n" +
                ")\n" +
                "order by z.工作部门,z.台账顺序,z.职工编码";

        println(_s)

        def row = db.rows(_s)
        return row
    }

    @RemotingInclude
    List queryJixiaoTaizhang(def map) {

        def _s = "select z.职工编码 as 工号,z.职工姓名 as 姓名, a.得分 as 绩效,b.得分 as 绩效大分,a.日期 from 职工 z\n" +
                "  left join dbo.BZHSA核算明细_个人得分 a on a.职工编码 = z.职工编码 and a.核算项目ID='$jixiao_id' and a.日期>='$map.begin' and a.日期<='$map.end' " +
                "  left join dbo.BZHSA核算明细_个人得分 b on b.职工编码 = z.职工编码 and b.核算项目ID='$jixiao_da_id' and a.日期 = b.日期 " +
                "  where   z.职工编码 = '$map.code'" +
                " order by a.日期";

        def row = db.rows(_s)
        return row
    }

    @RemotingInclude
    List queryAnquan(def map) {
        def _s = "select z.职工编码 as 工号,z.职工姓名 as 姓名, a.得分 as 安全 from 职工 z\n" +
                "  left join dbo.BZHSA核算明细_个人得分 a on a.职工编码 = z.职工编码 and a.核算项目ID='$anquan_id' and a.日期='$map.date'\n" +
                "   where   z.职工编码 in (\n" +
                " select 职工编码 from BZHSA核算明细_个人得分 where 职工编码 = '$map.usercode' or ( 部门编码='$map.code' and 日期='$map.date')\n" +
                ")\n" +
                "order by z.工作部门,z.台账顺序,z.职工编码";

        def row = db.rows(_s)
        return row
    }

    @RemotingInclude
    List queryAnquanTaizhang(def map) {

        def _s = "select z.职工编码 as 工号,z.职工姓名 as 姓名, a.得分 as 安全,a.日期 from 职工 z\n" +
                "  left join dbo.BZHSA核算明细_个人得分 a on a.职工编码 = z.职工编码 and a.核算项目ID='$anquan_id' and a.日期>='$map.begin' and a.日期<='$map.end' " +
                "  where   z.职工编码 = '$map.code'" +
                " order by a.日期";

        def row = db.rows(_s)
        return row
    }

    @RemotingInclude
    List queryJiangfa(def map) {
        def row = db.rows("select a.职工编码 as 工号,a.职工姓名 as 姓名,sum(isnull(a.得分,0)) as 得分,sum(isnull(a.金额,0)) as  金额 from BZHSA核算明细_个人奖罚 a\n" +
                "  left join 职工 z on a.职工编码 = z.职工编码\n" +
                " where  a.年='$map.year' and a.期='$map.month' and a.部门编码='$map.code'\n" +
                " group by a.年,a.期,a.职工编码,a.职工姓名,z.台账顺序\n" +
                " order by z.台账顺序,a.职工编码")
        return row
    }

    @RemotingInclude
    List queryJiangfaTaizhang(def map) {
        def row = db.rows("select 职工编码 as 工号,日期, 职工姓名 as 姓名,得分,金额,核算项目名称 as 奖罚项目 from  BZHSA核算明细_个人奖罚 where  日期>='$map.begin' and 日期<='$map.end' and 职工编码='$map.code'" +
                " order by 日期")
        return row
    }

    @RemotingInclude
    List queryGongzi(def map) {
        def row = db.rows("SELECT a.职工编码 AS 工号,a.职工姓名 AS 姓名,sum(a.合计金额) AS 工资金额 FROM BZHSA工资分配_明细_个人 a\n" +
                "  left join 职工 z on a.职工编码 = z.职工编码\n" +
                " WHERE a.列表ID=(  SELECT 列表ID FROM BZHSA工资分配_列表 WHERE 部门编码='" + getDuiCode(map) + "' AND 年='$map.year' AND 月='$map.month' and 是否审批结束=1)\n" +
                " and z.工作部门='$map.code'\n" +
                "  group by a.职工编码,a.职工姓名,z.台账顺序,a.工资顺序" +
                " ORDER BY z.台账顺序,a.工资顺序,a.职工编码")
        return row
    }

    @RemotingInclude
    List queryGongziTaizhang(def map) {
        def r = db.firstRow("select '加班工资' as 工资项目,isnull(加班工资,0) as 工资金额 from BZHSA工资分配_明细_个人 \n" +
                "  where  职工编码 = '$map.工号' \n" +
                "         and \n" +
                "         列表id = (  SELECT 列表ID FROM BZHSA工资分配_列表 WHERE 部门编码='" + getDuiCode(map) + "' AND 年='$map.year' AND 月='$map.month' and 是否审批结束=1)");



        List row = db.rows("""
select b.分配项目名称 as 工资项目,sum(a.最终分配金额) as 工资金额
from BZHSA工资分配_明细_项目数据 a
  left join BZHSA工资分配_明细_设置_分配项目 b on  b.分配项目设置ID=a.分配项目设置ID
  where a.明细ID in (
                select 个人明细ID from BZHSA工资分配_明细_个人
                  where  职工编码 = '$map.工号'
                         and
                         列表id = (  SELECT 列表ID FROM BZHSA工资分配_列表 WHERE 部门编码='${
            getDuiCode(map)
        }' AND 年='$map.year' AND 月='$map.month' and 是否审批结束=1)
                )
                and a.最终分配金额!= 0
group by b.分配项目名称,a.序号 order by a.序号
""")


        if (r != null && r.工资金额 != 0) {
            int year = Integer.parseInt(map.year + '')
            int month = Integer.parseInt(map.month + '')


            if (year < 2016) {
                for (Map item in row) {
                    if (item.工资项目 == '绩效') {
                        item.工资金额 = item.工资金额 - r.工资金额;
                    }
                }
            }

            row.add(0, r);
        }

        return row
    }

    @RemotingInclude
    List queryCailiao(def map) {
        def row = db.rows("""
SELECT
  SCHABMYDFYTZLB.[ID]
  ,   SCHABMYDFYTZLB.[年]
  ,   SCHABMYDFYTZLB.[月]
  ,   SCHABMYDFYTZLB.[部门编码]
  ,   SCHABMYDFYTZLB.[部门名称] as 部门
  ,   SCHABMYDFYTZLB.[收入金额合计] as 收入
  ,   SCHABMYDFYTZLB.[支出金额合计] as 支出
  ,   SCHABMYDFYTZLB.[结算余额] as 结余
  ,   SCHABMYDFYTZLB.[兑现金额] as 兑现
FROM SCHA班组月度费用汇总_列表   SCHABMYDFYTZLB
  LEFT JOIN (SELECT * FROM SCHA市场化结算部门列表 WHERE 类型 = '班组') JSBMLB ON JSBMLB.部门编码=SCHABMYDFYTZLB.部门编码
WHERE 1=1
      AND SCHABMYDFYTZLB.年=?
      AND SCHABMYDFYTZLB.月=?
       AND SCHABMYDFYTZLB.部门编码 in (SELECT 部门编码
                                  FROM 用户部门 WHERE  用户编码=?)
                                  and (SCHABMYDFYTZLB.[收入金额合计] > 0 or   SCHABMYDFYTZLB.[支出金额合计] >0)
ORDER BY SCHABMYDFYTZLB.结算年月,JSBMLB.序号,SCHABMYDFYTZLB.部门编码
""", [map.year, map.month, map.code])

        return row
    }

    @RemotingInclude
    List queryCailiaoTaizhang(def map) {
        def row = db.rows("""
SELECT
  SCHABMYDFYTZLB.年,SCHABMYDFYTZLB.月,
  SCHABMYDFYTZLB.部门编码,
  SCHABMYDFYTZLB.部门名称   as 部门
  ,SCHABMYDFYTZMX.费用明细编码
  ,SCHABMYDFYTZMX.费用明细名称 as 费用名称
  ,SCHABMYDFYTZMX.收入结算金额 as 收入
  ,SCHABMYDFYTZMX.支出结算金额 as 支出
  ,SCHABMYDFYTZMX.结算余额 as 结余
  ,SCHABMYDFYTZMX.兑现余额 as 兑现
FROM SCHA班组月度费用汇总_明细   SCHABMYDFYTZMX
  LEFT JOIN SCHA班组月度费用汇总_列表 SCHABMYDFYTZLB ON SCHABMYDFYTZLB.ID=费用汇总列表ID
WHERE 1=1
      AND SCHABMYDFYTZLB.年=?
      AND SCHABMYDFYTZLB.月=?
      AND SCHABMYDFYTZLB.部门编码 = ?
ORDER BY SCHABMYDFYTZMX.序号
""", [map.year, map.month, map.code])
        return row
    }

    @RemotingInclude
    Boolean isDateAllowKaoqin(Date date) {
        int x = Integer.parseInt(db.firstRow("SELECT isnull(参数值,0) AS x FROM config WHERE 参数='BZHSA控制打分考勤可以编辑服务器时间之前多少天的数据'").x);
        Date now = new Date()

        LocalDate start = new LocalDate(date)
        LocalDate end = new LocalDate(now);
        int days = Days.daysBetween(start, end).getDays();

        if (x <= 0)
            return true
        else
            return days <= x
    }

    @RemotingInclude
    Boolean isDateAllow(Date date) {
        int x = Integer.parseInt(db.firstRow("SELECT isnull(参数值,0) AS x FROM config WHERE 参数='BZHSA控制打分可以编辑服务器时间之前多少天的数据'").x);
        Date now = new Date()

        LocalDate start = new LocalDate(date)
        LocalDate end = new LocalDate(now);
        int days = Days.daysBetween(start, end).getDays();

        if (x <= 0)
            return true
        else
            return days <= x
    }

    @RemotingInclude
    Boolean testDatetime(Date date) {
        Date now = new Date()

        LocalDate start = new LocalDate(date)
        LocalDate end = new LocalDate(now);
        int min = Minutes.minutesBetween(start, end).getMinutes();
        return Math.abs(min) < 10
    }

    @RemotingInclude
    public void insertLog(LogVO logVO) {
        return;

//        String sNow = new Date().format('yyyyMMdd')
//        String tableName = "log_" + sNow
//        logdb.withTransaction {
//            logdb.execute("IF NOT EXISTS (SELECT * FROM dbo.sysobjects WHERE id = OBJECT_ID(N'[dbo].[" + tableName + "]') AND OBJECTPROPERTY(id, N'IsUserTable') = 1)\n" +
//                    "BEGIN\n" +
//                    "CREATE TABLE [dbo].[" + tableName + "](\n" +
//                    "\t[序号] [bigint] IDENTITY(1,1) NOT NULL,\n" +
//                    "\t[数据库名] [varchar](50) NULL,\n" +
//                    "\t[级别] [int] NULL,\n" +
//                    "\t[事件类型] [varchar](50) NULL,\n" +
//                    "\t[类别] [varchar](500) NULL,\n" +
//                    "\t[模块] [varchar](50) NULL,\n" +
//                    "\t[表名] [varchar](50) NULL,\n" +
//                    "\t[ID字段名] [varchar](50) NULL,\n" +
//                    "\t[ID值] [varchar](50) NULL,\n" +
//                    "\t[详细信息] [text] NULL,\n" +
//                    "\t[操作时间] [datetime] NULL,\n" +
//                    "\t[操作人登录名] [varchar](50) NULL,\n" +
//                    "\t[操作人姓名] [varchar](50) NULL,\n" +
//                    "\t[操作部门编码] [varchar](50) NULL,\n" +
//                    "\t[操作部门名称] [varchar](200) NULL,\n" +
//                    "\t[操作人IP] [varchar](50) NULL,\n" +
//                    "\t[操作人MAC] [varchar](50) NULL,\n" +
//                    "\t[操作计算机名] [varchar](200) NULL,\n" +
//                    "\t[操作系统用户名] [varchar](200) NULL,\n" +
//                    "\t[来源] [varchar](50) NULL,\n" +
//                    "\t[设备标识] [varchar](200) NULL,\n" +
//                    "\t[用时毫秒] [int] NULL\n" +
//                    ") ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]\n" +
//                    "END\n")
//        }
//
//        logdb.execute("insert " + tableName + "([数据库名], [级别], [事件类型], [类别], [模块], [表名], [ID字段名], [ID值], [详细信息], [操作时间], [操作人登录名], [操作人姓名], [操作部门编码], [操作部门名称], [操作人IP], [操作人MAC], [操作计算机名], [操作系统用户名], [来源], [设备标识], [用时毫秒])" +
//                "values ('$logVO.数据库名',' $logVO.级别',' $logVO.事件类型',' $logVO.类别',' $logVO.模块',' $logVO.表名',' $logVO.ID字段名',' $logVO.ID值',' $logVO.详细信息',' $logVO.操作时间',' $logVO.操作人登录名',' $logVO.操作人姓名',' $logVO.操作部门编码',' $logVO.操作部门名称',' $logVO.操作人IP',' $logVO.操作人MAC',' $logVO.操作计算机名',' $logVO.操作系统用户名',' $logVO.来源',' $logVO.设备标识',' $logVO.用时毫秒')")

    }


    class LogVO {
        String 数据库名 = dbname
        int 级别 = -1
        String 事件类型 = ""
        String 类别 = ""
        String 模块 = ""
        String 表名 = ""
        String ID字段名 = ""
        String ID值 = ""
        String 详细信息 = ""
        String 操作时间 = new Date().format("yyyy-MM-dd HH:mm:ss")
        String 操作人登录名 = ""
        String 操作人姓名 = ""
        String 操作部门编码 = ""
        String 操作部门名称 = ""
        String 操作人IP = ""
        String 操作人MAC = ""
        String 操作计算机名 = ""
        String 操作系统用户名 = "root"
        String 来源 = "手机端"
        String 设备标识 = ""
        int 用时毫秒 = 0

        LogVO() {}

        LogVO(Map map) {
            操作人登录名 = map.登录名
            设备标识 = map.设备标识
            操作人姓名 = map.用户姓名
            操作部门名称 = map.部门名称 ?: ''
            操作部门编码 = map.工作部门 ?: ''
        }
    }
}

