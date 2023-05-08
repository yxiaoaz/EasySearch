package DataWareHouse;

import jdbm.helper.FastIterator;
import jdbm.htree.HTree;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class FileManager {
    private HashMap<String, IndexFile> fileRecords;
    private HashMap<String, Long> recmanID;
    private AtomicInteger numOfDoc=new AtomicInteger();
    private AtomicInteger numOfTerm=new AtomicInteger();
    private AtomicInteger numOfTitleTerm=new AtomicInteger();
    public FileManager(){
        recmanID = new HashMap<>();
        fileRecords = new HashMap<>();
    }
    /**Build a file manager to manager established database*/
    public FileManager(String record) throws IOException {
        recmanID = new HashMap<>();
        fileRecords = new HashMap<>();
        Scanner scan = new Scanner(new File(record));
        while(scan.hasNextLine()) {
            String[] entry = scan.nextLine().split(" ");
            recmanID.put(entry[0], Long.valueOf(entry[1]));
        }
        for(String filename: recmanID.keySet()){
            loadIndexFile(filename);
        }
        HTree docrecords = fileRecords.get(FileNameGenerator.DOCRECORDS).getFile();
        FastIterator iterator = docrecords.keys();
        while(iterator.next()!=null){
            numOfDoc.set(numOfDoc.get()+1);
        }
        HTree invertedIndex = fileRecords.get(FileNameGenerator.INVERTEDINDEX).getFile();
        FastIterator iterator2 = docrecords.keys();
        while(iterator2.next()!=null){
            numOfTerm.set(numOfTerm.get()+1);
        }
        HTree titleInvertedIndex = fileRecords.get(FileNameGenerator.TITLEINVERTEDINDEX).getFile();
        FastIterator iterator3 = docrecords.keys();
        while(iterator3.next()!=null){
            numOfTitleTerm.set(numOfTitleTerm.get()+1);
        }
    }
    public void storeRecmanID(String filename) throws IOException {
        BufferedWriter a = new BufferedWriter(new FileWriter(filename));
        Set<String> keys = recmanID.keySet();
        for(String key: keys) {
            a.write(key+" "+recmanID.get(key));
            a.newLine();
        }
        a.close();
    }
    public int getNumOfDoc(){return numOfDoc.get();}
    public int getNumOfTerm(){return numOfTerm.get();}
    public void numDocAddOne(){numOfDoc.set(numOfDoc.get()+1);}
    public void numTermAddOne(){numOfTerm.set(numOfTerm.get()+1);}
    public void numTitleTermAddOne(){numOfTitleTerm.set(numOfTitleTerm.get()+1);}
    public void loadIndexFile(String name) throws IOException {
        IndexFile newFile = new IndexFile(name,this, recmanID.get(name));
        addIndexFile(name,newFile);
    }
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
        recmanID.put(fileName,file.getRecmanID());
    }
}
