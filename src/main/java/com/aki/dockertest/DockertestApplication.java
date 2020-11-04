package com.aki.dockertest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@SpringBootApplication
@Controller
public class DockertestApplication {
    @Autowired
    UserRepository userRepository;

    public static void main(String[] args) {
        SpringApplication.run(DockertestApplication.class, args);
    }

    @RequestMapping(value = "add")
    @ResponseBody
    public String  addUsers(){
        User user = new User();
        user.setUserName("张三");
        user.setUserAge(20);
        user.setUserAddress("北京");
        userRepository.save(user);
        return "成功添加了张三";
    }

    @RequestMapping(value = "get")
    @ResponseBody
    public String  getUser(){
        List<User> all = userRepository.findAll();
        return all.toString();
    }
}
