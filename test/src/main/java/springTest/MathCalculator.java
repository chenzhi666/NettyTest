package springTest;

import org.springframework.stereotype.Component;

@Component
public class MathCalculator {

    public MathCalculator(){
        System.out.println("MathCalculator 创建成功...");
        div(1,1);
    }

    public int div(int i,int j){
        System.out.println("MathCalculator...div...");
        return i/j;
    }
}
