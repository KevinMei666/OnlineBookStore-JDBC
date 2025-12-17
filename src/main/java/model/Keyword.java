package model;

public class Keyword {

    private Integer keywordId;
    private String word;

    public Keyword() {
    }

    public Keyword(Integer keywordId, String word) {
        this.keywordId = keywordId;
        this.word = word;
    }

    public Integer getKeywordId() {
        return keywordId;
    }

    public void setKeywordId(Integer keywordId) {
        this.keywordId = keywordId;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }
}


