package domain;


import java.io.Serializable;

/**
 * 广告推广渠道
 * Created by sibyl.sun on 16/9/5.
 */
public class Ad implements Serializable {
    private Long id;
    private String              adSource;//广告来源：0118亿起发
    private String              subAdSource;//每个广告的不同的推广方式
    private String              adParam;//参数

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAdSource() {
        return adSource;
    }

    public void setAdSource(String adSource) {
        this.adSource = adSource;
    }

    public String getSubAdSource() {
        return subAdSource;
    }

    public void setSubAdSource(String subAdSource) {
        this.subAdSource = subAdSource;
    }

    public String getAdParam() {
        return adParam;
    }

    public void setAdParam(String adParam) {
        this.adParam = adParam;
    }

    @Override
    public String toString() {
        return "Ad{" +
                "id=" + id +
                ", adSource='" + adSource + '\'' +
                ", subAdSource='" + subAdSource + '\'' +
                ", adParam='" + adParam + '\'' +
                '}';
    }
}
