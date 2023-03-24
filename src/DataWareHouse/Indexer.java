package DataWareHouse;
import IRUtilities.StopStem;
import org.jsoup.Jsoup;
import jdbm.htree.HTree;
import org.htmlparser.beans.StringBean;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.StringTokenizer;

public class Indexer {
    //The database the indexer is serving
    private FileManager fileManager;
    private static StopStem stopStem;

    /**
     * Code of Conduct: Create FileManager-> affiliate indexer to FileManager
     * */
    public Indexer(FileManager fileManager) throws IOException {
        this.fileManager = fileManager;
        if(stopStem == null)
            stopStem = new StopStem("stopwords.txt");
    }

    /**
     * The driving method of the whole processing action
     @param url: where to fetch all the info from
     inverted index files grouped by first letter of the term
      * */
    public void processContent(URL url) throws IOException {
        /**Step 1: save the URL to the docRecords*/
        // When we reach this stage, the URL is already valid to record
        String docID = ID_Mapping.URL2ID(url);
        HTree docRecords = fileManager.getIndexFile(FileNameGenerator.getDocRecordsName(url)).getFile();
        Date lastModified = url.openConnection().getLastModified()==0?
                new Date(url.openConnection().getLastModified())
                :new Date(url.openConnection().getDate());
        String title = Jsoup.connect(url.toString()).get().title();
        int size = Jsoup.connect(url.toString()).get().body().text().length();

        DocProfile profile = new DocProfile(ID_Mapping.URL2ID(url),lastModified,title,size);
        docRecords.put(docID,profile);

        /**Step 2: fetch terms and update inverted index & forward index
         *         fetch links and update webgraph+
         **/
        ArrayList<String> terms = extractTerms(url);
        for(int i=0;i< terms.size();i++){
            String t = terms.get(i);
            indexTerm(t,i,url);
            indexDoc(t,url);
        }

    }

    /**
     * @return a list of stemmed terms extracted from the url
     * */
    public ArrayList<String> extractTerms(URL url) throws IOException {
        ArrayList<String> res = new ArrayList<>();

        Document doc = Jsoup.connect(url.toString()).get();
        String text = doc.body().text();
        StringTokenizer st = new StringTokenizer(text);
        while (st.hasMoreTokens()) {
            String t = st.nextToken();
            if(!stopStem.isStopWord(t))
                res.add(stopStem.stem(t));
        }
        return res;
    }

    /**
     * @param url : the link from which child links are extracted
     *
     * Task:
     *
     * */
    public ArrayList<URL> extractLinks(URL url) throws IOException {
        ArrayList<URL> v_link = new ArrayList<URL>();
        //Get Document object after parsing the html from given url.
        Document document = Jsoup.connect(url.toString()).get();

        //Get links from document object.
        Elements links = document.select("a[href]");

        for(int i=0;i< links.size();i++){
            //System.out.println(links.get(i).attr("abs:href"));
            try{
                URL extracted = new URL(links.get(i).attr("abs:href"));
                updateLinkGraph(url,extracted);
                v_link.add(extracted);
            }catch(MalformedURLException e){
                continue;
            }

        }
        return v_link;
    }

    public void updateLinkGraph(URL parent, URL child) throws IOException {
        HTree parent2child = fileManager.getIndexFile(FileNameGenerator.getWebGraphName_parent2child(parent)).getFile();
        HTree child2parent = fileManager.getIndexFile(FileNameGenerator.getWebGraphName_child2parent(child)).getFile();
        String parentID = ID_Mapping.URL2ID(parent);
        String childID = ID_Mapping.URL2ID(child);

        ArrayList<String> childrenList;
        /**if there is no entry for this parent yet*/
        if(parent2child.get(parentID)==null){
            childrenList = new ArrayList<>();
            childrenList.add(childID);
        }
        else{
            childrenList = (ArrayList<String>) parent2child.get(parentID);
            if(!childrenList.contains(childID))
                childrenList.add(childID);
        }
        parent2child.put(parentID,childrenList);

        ArrayList<String> parentList;
        /**if there is no entry for this parent yet*/
        if(child2parent.get(childID)==null){
            parentList = new ArrayList<>();
            parentList.add(parentID);
        }
        else{
            parentList = (ArrayList<String>) child2parent.get(childID);
            if(!parentList.contains(parentID))
                parentList.add(parentID);
        }
        child2parent.put(childID,parentList);



    }


    public boolean validURL(URL url) throws IOException {
        HTree docRecords = fileManager.getIndexFile(FileNameGenerator.getDocRecordsName(url)).getFile();
        if(docRecords.get(ID_Mapping.URL2ID(url))!=null){
            DocProfile docProfile = (DocProfile) docRecords.get(ID_Mapping.URL2ID(url));
            Date recordedModifiedDate = docProfile.getLastModified();

            //the last modified date of this page
            // if ==0, use the "date" field instead
            Date lastModified = url.openConnection().getLastModified()==0?
                    new Date(url.openConnection().getLastModified())
                    :new Date(url.openConnection().getDate());
            if(!lastModified.after(recordedModifiedDate))
                return false;
        }
        return true;
    }

    /**
     * Index a Single Term (not mapped to ID)
     * */
    public void indexTerm(String term, int position, URL url) throws IOException {
        HTree invertedIndex = fileManager.getIndexFile(FileNameGenerator.getInvertedIndexFileName(term)).getFile();
        String termID = ID_Mapping.Term2ID(term);
        String docID = ID_Mapping.URL2ID(url);

        // acquire(construct new) corresponding posting list
        ArrayList<IIPosting> postingList;
        if(invertedIndex.get(termID)==null){
            postingList = new ArrayList<>();

            //to be included in a IIPosting: which document, where in the document
            IIPosting newPosting = new IIPosting(docID);
            newPosting.addPosition(position);
            postingList.add(newPosting);
        }
        else{
            postingList = (ArrayList<IIPosting>) invertedIndex.get(termID);
            IIPosting oldPosting= new IIPosting(docID);;
            for(int j = 0;j<postingList.size();j++){
                if (postingList.get(j).getID().equals(docID)){
                    oldPosting = postingList.get(j);
                    break;
                }
            }

            oldPosting.addPosition(position);
            postingList.add(oldPosting);
        }

        invertedIndex.put(termID,postingList);
    }

    /**
     * @param url : the key for which the FORWARD INDEX posting is updated
     * @param term : the occurence of term in the url to update
     * */
    public void indexDoc(String term, URL url) throws IOException {
        HTree forwardIndex = fileManager.getIndexFile(FileNameGenerator.getForwardIndexFileName(url)).getFile();
        String termID = ID_Mapping.Term2ID(term);
        String docID = ID_Mapping.URL2ID(url);

        ArrayList<FIPosting> postingList;
        if(forwardIndex.get(docID)==null){// first time index this doc into the forward index
            postingList = new ArrayList<>();
            FIPosting newPosting = new FIPosting(termID);
            newPosting.addOccurence();
            postingList.add(newPosting);
        }
        else{//URL already has key in forward index
            postingList = (ArrayList<FIPosting>) forwardIndex.get(docID);
            FIPosting oldPosting= new FIPosting(termID);;
            for(int j = 0;j<postingList.size();j++){
                if (postingList.get(j).getID().equals(termID)){
                    oldPosting = postingList.get(j);
                    break;
                }
            }

            oldPosting.addOccurence();
            postingList.add(oldPosting);
        }
        forwardIndex.put(docID,postingList);

    }

}
