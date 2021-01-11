package listener;

public class MyListener {

    public  String collectFinished(String result){
        System.out.println("异步收集完成："+result);
        return  result;
    }
}
