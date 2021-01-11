import java.util.*;
public class Main{
    public static void main(String[] arags){
        Scanner sc=new Scanner(System.in);
        List<String> list=new ArrayList<String>();
        int n=sc.nextInt();
        for(int i=0;i<n;i++){
            list.add(sc.next());
        }
        Collections.sort(list, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                long s1=exchange(o1);
                long s2=exchange(o2);
                if (s1==s2){
                    return 0;
                }else {
                    return  s1>s2?1:-1;
                }
            }
            //转换数字,统一单位M
            public  long exchange(String str){
                int size=str.length();
                long s=Long.parseLong(str.substring(0,size-1));
                if(str.contains("M")){
                    return s;
                }
                if(str.contains("G")){
                    long g=s*1000;
                    return g;
                }
                if(str.contains("T")){
                    long t=s*1000*1000;
                    return t;
                }
                return 0;
            }

        });
        list.forEach(System.out::println);
    }


}