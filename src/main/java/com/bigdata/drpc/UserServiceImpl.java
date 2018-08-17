package com.bigdata.drpc;

/**
 * Created by luozhenfei1 on 2018/6/29.
 * 用户服务接口实现类
 */
public class UserServiceImpl implements UserService {

    public void addUser(String name, int age) {
        System.out.println("From Server Invoked: add user success......,name is " + name);
    }
}
