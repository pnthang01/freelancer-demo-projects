package com.etybeno.openrtb.model;

import java.util.Map;

/**
 * Created by thangpham on 16/04/2018.
 */
public class BidRequestModel {

    private Map<String, String> metadata;
    private ResultsModel results;

    public String getResultUrl() {
        if(results != null && results.getResult() != null) return results.getResult().getUrl();
        else return null;
    }

    public double getResultBid() {
        if(results != null && results.getResult() != null) return results.getResult().getBid();
        else return 0;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    public ResultsModel getResults() {
        return results;
    }

    public void setResults(ResultsModel results) {
        this.results = results;
    }

    public static class ResultsModel{
        private int first;
        private int last;
        private int total;
        private ResultModel result;

        public int getFirst() {
            return first;
        }

        public void setFirst(int first) {
            this.first = first;
        }

        public int getLast() {
            return last;
        }

        public void setLast(int last) {
            this.last = last;
        }

        public int getTotal() {
            return total;
        }

        public void setTotal(int total) {
            this.total = total;
        }

        public ResultModel getResult() {
            return result;
        }

        public void setResult(ResultModel result) {
            this.result = result;
        }
    }

    public static class ResultModel {
        private String url;
        private double rpm;
        private double bid;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public double getRpm() {
            return rpm;
        }

        public void setRpm(double rpm) {
            this.rpm = rpm;
        }

        public double getBid() {
            return bid;
        }

        public void setBid(double bid) {
            this.bid = bid;
        }
    }
}
