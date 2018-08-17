package com.bigdata.drpc;

/**
 * Created by luozhenfei1 on 2018/6/29.
 * 用户服务接口
 */
public interface UserService {
    public static final long versionID=1l;

    /*
    * 添加用户明细年龄
    * */
    public void addUser(String name, int age);
}
