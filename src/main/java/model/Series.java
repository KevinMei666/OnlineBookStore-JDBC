package model;

import java.time.LocalDateTime;

public class Series {
    
    private Integer seriesId;
    private String seriesName;
    private String description;
    private String publisher;
    private LocalDateTime createDate;
    
    public Series() {
    }
    
    public Series(Integer seriesId, String seriesName, String description, 
                  String publisher, LocalDateTime createDate) {
        this.seriesId = seriesId;
        this.seriesName = seriesName;
        this.description = description;
        this.publisher = publisher;
        this.createDate = createDate;
    }
    
    public Integer getSeriesId() {
        return seriesId;
    }
    
    public void setSeriesId(Integer seriesId) {
        this.seriesId = seriesId;
    }
    
    public String getSeriesName() {
        return seriesName;
    }
    
    public void setSeriesName(String seriesName) {
        this.seriesName = seriesName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getPublisher() {
        return publisher;
    }
    
    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }
    
    public LocalDateTime getCreateDate() {
        return createDate;
    }
    
    public void setCreateDate(LocalDateTime createDate) {
        this.createDate = createDate;
    }
}

