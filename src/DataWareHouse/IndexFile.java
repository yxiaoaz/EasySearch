package DataWareHouse;
import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.htree.HTree;

import java.io.IOException;


public class IndexFile {

    private RecordManager recman;
    private String name;
    private HTree file;
    private FileManager fileManager;
    /*
    *Create a database with specified record manager name
    *For simplicity, the Database shares the same name.
    **/

    /**Create a new index file for the first time*/
    public IndexFile(String name, FileManager fileManager) throws IOException {
        this.name = name;
        this.recman = RecordManagerFactory.createRecordManager(name);
        file = HTree.createInstance(recman);
        recman.setNamedObject( name, file.getRecid());

        this.fileManager = fileManager;
    }
    /**Create a new index file for an existed HTree*/
    public IndexFile(String name, FileManager fileManager, long recid) throws IOException {
        this.name = name;
        this.recman = RecordManagerFactory.createRecordManager(name);
        file = HTree.load(recman, recid);
        this.fileManager = fileManager;
    }
    public long getRecmanID(){
        return this.file.getRecid();
    }
    public void saveChanges() throws IOException {
        recman.commit();
    }
    public void close() throws IOException {
        recman.close();
    }
    public HTree getFile(){
        return file;
    }


}
