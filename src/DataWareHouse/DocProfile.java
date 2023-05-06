package DataWareHouse;

import DataWareHouse.ID_Mapping;
import DataWareHouse.Posting;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Date;

public class DocProfile extends Posting {
    private String title;
    ArrayList<String> stemmedTitle = new ArrayList<>();
    private Date lastModified;
    private int size;

    public DocProfile(String ID, Date lastModified, String title) {
        super(ID);
        this.lastModified = lastModified;
        this.title = title;

    }

    public DocProfile(String ID, Date lastModified, String title, int size) {
        this(ID, lastModified, title);
        this.size = size;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void updateModifiedDate(Date newdate) {
        lastModified = newdate;
    }
    public void addStemmedTitleTerm(String term){stemmedTitle.add(term);}
    public String getTitle() {
        return title;
    }
    public ArrayList<String> getStemmedTitle(){return stemmedTitle;}
    public String getURLinString() throws MalformedURLException {
        return ID_Mapping.PageID2URL(ID).toString();
    }

    public int getSize(){
        return this.size;
    }
}