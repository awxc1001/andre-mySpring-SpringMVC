package com.andre.controller;

import com.andre.andrespringmvc.annotation.*;
import com.andre.entity.Monster;
import com.andre.service.MonsterService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * @author Andre Wang
 * @version 1.0
 */
@Controller
public class MonsterController {

    //@AutoWired表示要完成属性的装配.
    @AutoWired("myService")
    private MonsterService monsterService;

    //编写方法,可以列出妖怪列表
    //springmvc 是支持原生的servlet api, 为了看到底层机制
    @RequestMapping(value = "/monster/list")
    public void listMonster(HttpServletRequest request,
                            HttpServletResponse response) {
        //设置编码和返回类型
        response.setContentType("text/html;charset=utf-8");

        StringBuilder content = new StringBuilder("<h1>妖怪列表信息</h1>");
        //调用monsterService
        List<Monster> monsters = monsterService.listMonster();
        content.append("<table border='1px' width='500px' style='border-collapse:collapse'>");
        for (Monster monster : monsters) {
            content.append("<tr><td>" + monster.getId()
                    + "</td><td>" + monster.getName() + "</td><td>"
                    + monster.getSkill() + "</td><td>"
                    + monster.getAge() + "</td></tr>");
        }
        content.append("</table>");

        //获取writer返回信息
        try {
            PrintWriter printWriter = response.getWriter();
            printWriter.write(content.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //增加方法，通过name返回对应的monster集合

    @RequestMapping(value = "/monster/find")
    public void findMonsterByName(HttpServletRequest request,
                                  HttpServletResponse response,
                                  @RequestParam("name") String name /**这里什么都可以，只要注解和param对应**/) {
        //设置编码和返回类型
        response.setContentType("text/html;charset=utf-8");
        System.out.println("--received requestParam name---" + name);
        StringBuilder content = new StringBuilder("<h1>妖怪列表信息</h1>");
        //调用monsterService
        List<Monster> monsters = monsterService.findMonsterByName(name);
        content.append("<table border='1px' width='400px' style='border-collapse:collapse'>");
        for (Monster monster : monsters) {
            content.append("<tr><td>" + monster.getId()
                    + "</td><td>" + monster.getName() + "</td><td>"
                    + monster.getSkill() + "</td><td>"
                    + monster.getAge() + "</td></tr>");
        }
        content.append("</table>");

        //获取writer返回信息
        try {
            PrintWriter printWriter = response.getWriter();
            printWriter.write(content.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

     @RequestMapping(value = "/monster/find2")
    public void findMonsterByName(HttpServletRequest request,
                                  HttpServletResponse response,
                                  String name,String age /**这里什么都可以，只要注解和param对应**/) {
        //设置编码和返回类型
        response.setContentType("text/html;charset=utf-8");
        System.out.println("--received requestParam name---" + name);
        StringBuilder content = new StringBuilder("<h1>妖怪列表信息</h1>");
        //调用monsterService
        List<Monster> monsters = monsterService.findMonsterByName(name);
        content.append("<table border='1px' width='400px' style='border-collapse:collapse'>");
        for (Monster monster : monsters) {
            content.append("<tr><td>" + monster.getId()
                    + "</td><td>" + monster.getName() + "</td><td>"
                    + monster.getSkill() + "</td><td>"
                    + monster.getAge() + "</td></tr>");
        }
        content.append("</table>");

        //获取writer返回信息
        try {
            PrintWriter printWriter = response.getWriter();
            printWriter.write(content.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //处理妖怪登录的方法,返回要请求转发/重定向的字符串
    @RequestMapping("/monster/login")
    public String login(HttpServletRequest request,
                        HttpServletResponse response,
                        String monsterName) {//same as frontend name

        System.out.println("--monsterName---" + monsterName);
        //将mName设置到request域
        request.setAttribute("monsterName", monsterName);
        boolean b = monsterService.login(monsterName);
        if (b) {//登录成功!
            //return "forward:/login_ok.jsp";
            //测试重定向
            //return "redirect:/login_ok.jsp";
            //测试默认的方式-forward
            return "/login_ok.jsp";

        } else {//登录失败
            return "forward:/login_error.jsp";
        }
    }

    /**
     * 编写方法,返回json格式的数据
     * 1. 梳理
     * 2. 目标方法返回的结果是给springmvc底层通过反射调用的位置
     * 3. 在springmvc底层反射调用的位置，接收到结果并解析即可
     * 4. 方法上标注了 @ResponseBody 表示希望以json格式返回给客户端/浏览器
     * 5. 目标方法的实参，在springmvc底层通过封装好的参数数组，传入..
     * @param request
     * @param response
     * @return
     */
    @RequestMapping("/monster/list/json")
    @ResponseBody
    public List<Monster> listMonsterByJson(HttpServletRequest request,
                                           HttpServletResponse response) {

        List<Monster> monsters = monsterService.listMonster();
        return monsters;
    }
}
