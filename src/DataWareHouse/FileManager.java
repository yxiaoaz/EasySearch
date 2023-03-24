package DataWareHouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class FileManager {
    private HashMap<String, IndexFile> fileRecords;
    public FileManager(){
        fileRecords = new HashMap<>();
    }
    public void createIndexFile(String name) throws IOException {
        IndexFile newFile = new IndexFile(name,this);
        addIndexFile(name,newFile);
    }
    public IndexFile getIndexFile(String fileName){
        return fileRecords.get(fileName);
    }
    public ArrayList<IndexFile> getAllIndexFiles(){
        ArrayList<IndexFile> res = new ArrayList<IndexFile>(fileRecords.values());
        return res;
    }
    public void closeAllFiles() throws IOException {
        for(IndexFile i:getAllIndexFiles()){
            i.close();
        }
    }
    public void addIndexFile(String fileName, IndexFile file){
        fileRecords.put(fileName,file);
    }
}
