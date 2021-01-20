# dubbo-hmily-demo
## 数据库
- ### 需要用到3个数据库：详细sql请看（demo.sql）
   * 1、hmily tcc存储柔性事务信息的数据库：参考hmily.yml配置（jdbc:mysql://127.0.0.1:3306/hmily）
   * 2、账户A人民币数据所在的数据库hmily_fx1
   * 3、账户A美元数据所在的数据库hmily_fx2
   * 4、账户表及初始化数据
## 模块介绍
- ### boot-provider
   提供dubbo接口，供（boot-consumer）调用，实现对账户美元数据的操作（hmily_fx2）
- ### boot-consumer
   测试接口：http://127.0.0.1:9000/fx (post请求)
   * 参数：raw json
       {
   	      "user_id": 2,
    	      "amount": 1,
             "buySellFlag" : false
    }
   * 用户2，兑换1美元-人民币
      
## 测试结果
 - ### boot-consumer fx接口本地开启hmily tcc事务，远程服务boot-provider也开启hmily tcc事务；当远程服务事务失败，或者fx本地事务抛异常时，事务回滚
 - ### 回滚情况
    * 1、fx本地成功，provider远程成功：执行confirm方法
    * 2、fx本地失败，provider远程成功：fx立即执行cancel方法；provider事务不会confirm，会超时取消（可以provider方法加断点，fx本地就会超时失败cancel，这个时候provider是成功的）
    * 3、fx本地失败，provider远程失败：fx、provider事务都立即回滚
