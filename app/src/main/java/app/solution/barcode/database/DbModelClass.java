package app.solution.barcode.database;

/**
 * Created by toukirul on 30/1/2018.
 */

public class DbModelClass {

    private int id;
    private String scanQuery;
    private String title;
    private String dateTime;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    public String getScanQuery() {
        return scanQuery;
    }

    public void setScanQuery(String scanQuery) {
        this.scanQuery = scanQuery;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }
}
