package springTest;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

//@Component
public class Car implements ApplicationContextAware {
    @Autowired
    private Apple apple;
    public Car(){
        System.out.println("car 无参构造。。。");
    }
    @PostConstruct
    public void init(){
        System.out.println("car ... init...");
    }
    @PreDestroy
    public void detory(){
        System.out.println("car ... detory...");
    }

    public Apple getApple() {
        return apple;
    }

    public void setApple(Apple apple) {
        System.out.println("setApple注入。。。。");
        this.apple = apple;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        System.out.println("setApplicationContext car 获取上下文");
    }
}
