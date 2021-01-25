package springTest;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;


@Configuration
@EnableAspectJAutoProxy
@ComponentScan(value = "springTest")
public class TestConfig {
    public static void main(String[] args) {
        ApplicationContext context = new AnnotationConfigApplicationContext(TestConfig.class);
        String[] definitionNames = context.getBeanDefinitionNames();
        for (String definitionName : definitionNames) {
            //System.out.println(definitionName);
        }
        MathCalculator mathCalculator=context.getBean(MathCalculator.class);
        mathCalculator.div(1,1);
    }
}

