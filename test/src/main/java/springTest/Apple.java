package springTest;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Component
public class Apple implements ApplicationContextAware {
    @Autowired
    private  Car car;
    public Apple(){
        System.out.println("apple 创建。。。");
    }

    public Car getCar() {
        return car;
    }

    @PostConstruct
    public void init(){
        System.out.println("PostConstruct Apple ... init...");
    }
    @PreDestroy
    public void detory(){
        System.out.println("PreDestroy Apple ... detory...");
    }


    public void setCar(Car car) {
        System.out.println("setCar.....");
        this.car = car;
    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        System.out.println();
    }
}
