package DataWareHouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class FileManager {
    private HashMap<String, IndexFile> fileRecords;
    private AtomicInteger numOfDoc=new AtomicInteger();
    private AtomicInteger numOfTerm=new AtomicInteger();
    private AtomicInteger numOfTitleTerm=new AtomicInteger();
    public FileManager(){
        fileRecords = new HashMap<>();
    }
    public int getNumOfDoc(){return numOfDoc.get();}
    public int getNumOfTerm(){return numOfTerm.get();}
    public void numDocAddOne(){numOfDoc.set(numOfDoc.get()+1);}
    public void numTermAddOne(){numOfTerm.set(numOfTerm.get()+1);}
    public void numTitleTermAddOne(){numOfTitleTerm.set(numOfTitleTerm.get()+1);}
    public void createIndexFile(String name) throws IOException {
        IndexFile newFile = new IndexFile(name,this);
        addIndexFile(name,newFile);
    }
    public IndexFile getIndexFile(String fileName){
        return fileRecords.get(fileName);
    }
    public boolean fileExists(String filename){
        return fileRecords.containsKey(filename);
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
    public void saveAllFiles() throws IOException {
        for(IndexFile i:getAllIndexFiles()){
            i.saveChanges();
        }
    }
    public void addIndexFile(String fileName, IndexFile file){
        fileRecords.put(fileName,file);
    }
}
