package CacheRecorder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class CacheRecorder {
    private  final static  String filename="esRecord.data";
    private  final static  CacheRecorder RECORDER=new CacheRecorder();
    private RandomAccessFile randomAccessFile;
    //数据缓存记录对象
    private DataRecord dataRecord;

    public static CacheRecorder getRecorder(){
        return  RECORDER;
    }

    public void  initRandomAccessFile(){
        if (randomAccessFile==null){
            try {
                randomAccessFile=new RandomAccessFile(filename,"rw");
            } catch (FileNotFoundException e) {
                new File(filename);
            }
        }
    }
    public void read(){
        initRandomAccessFile();

    }

    public void write(DataRecord dataRecord) {
        //更新缓存对象
        this.dataRecord=dataRecord;
        initRandomAccessFile();
        //保存写入文件
        try {
            //重写
            randomAccessFile.setLength(0);
            randomAccessFile.writeInt(dataRecord.getRecord());
            randomAccessFile.writeChars("/n"+dataRecord.getFrom());
            randomAccessFile.writeChars("/n"+dataRecord.getTo());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
