package DataWareHouse;

import java.util.ArrayList;
import java.util.HashMap;

public class FileManager {
    private HashMap<String, IndexFile> fileRecords;
    public FileManager(){
        fileRecords = new HashMap<>();
    }
    public IndexFile getIndexFile(String fileName){
        return fileRecords.get(fileName);
    }

    public void addIndexFile(String fileName, IndexFile file){
        fileRecords.put(fileName,file);
    }
}
