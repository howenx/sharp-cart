package domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Theme table module
 * Created by howen on 15/10/26.
 */
public class Theme implements Serializable {

    private static final long serialVersionUID = 21L;

    @JsonIgnore
    private     Long        id;//主题ID
    private     String      themeImg;//主题图片
    private     String      themeUrl;//跳转到主题列表页的链接
    private     String      type;//主题类型

    @JsonIgnore
    private     String      title;
    @JsonIgnore
    private     Timestamp   startAt;
    @JsonIgnore
    private     Timestamp   endAt;
    @JsonIgnore
    private     Integer     sortNu;
    @JsonIgnore
    private     Boolean     orDestroy;
    @JsonIgnore
    private     Timestamp   destroyAt;
    @JsonIgnore
    private     Timestamp   updateAt;
    @JsonIgnore
    private     Timestamp   createAt;
    @JsonIgnore
    private     String      themeSrcImg;
    @JsonIgnore
    private     String    themeConfigInfo;
    @JsonIgnore
    private     String    themeItem;
    @JsonIgnore
    private     String    masterItemTag;
    @JsonIgnore
    private     Long        masterItemId;

    @JsonIgnore
    private     String      themeMasterImg;

    @JsonIgnore
    private     Integer     themeNum;


    @JsonIgnore
    private     String      h5Link;
    /**
     * 主题状态（1隐藏 2专用 3正常）
     */
    @JsonIgnore
    private  Integer themeState;

    @JsonIgnore
    private Integer pageSize;
    @JsonIgnore
    private Integer offset;
    @JsonIgnore
    private Integer themeCateCode; //1.首页，2.拼购，3.礼品

    public Theme() {
    }

    public Theme(Long id, String themeImg, String themeUrl, String type, String title, Timestamp startAt, Timestamp endAt, Integer sortNu, Boolean orDestroy, Timestamp destroyAt, Timestamp updateAt, Timestamp createAt, String themeSrcImg, String themeConfigInfo, String themeItem, String masterItemTag, Long masterItemId, String themeMasterImg, Integer themeNum, String h5Link) {
        this.id = id;
        this.themeImg = themeImg;
        this.themeUrl = themeUrl;
        this.type = type;
        this.title = title;
        this.startAt = startAt;
        this.endAt = endAt;
        this.sortNu = sortNu;
        this.orDestroy = orDestroy;
        this.destroyAt = destroyAt;
        this.updateAt = updateAt;
        this.createAt = createAt;
        this.themeSrcImg = themeSrcImg;
        this.themeConfigInfo = themeConfigInfo;
        this.themeItem = themeItem;
        this.masterItemTag = masterItemTag;
        this.masterItemId = masterItemId;
        this.themeMasterImg = themeMasterImg;
        this.themeNum = themeNum;
        this.h5Link = h5Link;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getThemeImg() {
        return themeImg;
    }

    public void setThemeImg(String themeImg) {
        this.themeImg = themeImg;
    }

    public String getThemeUrl() {
        return themeUrl;
    }

    public void setThemeUrl(String themeUrl) {
        this.themeUrl = themeUrl;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Timestamp getStartAt() {
        return startAt;
    }

    public void setStartAt(Timestamp startAt) {
        this.startAt = startAt;
    }

    public Timestamp getEndAt() {
        return endAt;
    }

    public void setEndAt(Timestamp endAt) {
        this.endAt = endAt;
    }

    public Integer getSortNu() {
        return sortNu;
    }

    public void setSortNu(Integer sortNu) {
        this.sortNu = sortNu;
    }

    public Boolean getOrDestroy() {
        return orDestroy;
    }

    public void setOrDestroy(Boolean orDestroy) {
        this.orDestroy = orDestroy;
    }

    public Timestamp getDestroyAt() {
        return destroyAt;
    }

    public void setDestroyAt(Timestamp destroyAt) {
        this.destroyAt = destroyAt;
    }

    public Timestamp getUpdateAt() {
        return updateAt;
    }

    public void setUpdateAt(Timestamp updateAt) {
        this.updateAt = updateAt;
    }

    public Timestamp getCreateAt() {
        return createAt;
    }

    public void setCreateAt(Timestamp createAt) {
        this.createAt = createAt;
    }

    public String getThemeSrcImg() {
        return themeSrcImg;
    }

    public void setThemeSrcImg(String themeSrcImg) {
        this.themeSrcImg = themeSrcImg;
    }

    public String getThemeConfigInfo() {
        return themeConfigInfo;
    }

    public void setThemeConfigInfo(String themeConfigInfo) {
        this.themeConfigInfo = themeConfigInfo;
    }

    public String getThemeItem() {
        return themeItem;
    }

    public void setThemeItem(String themeItem) {
        this.themeItem = themeItem;
    }

    public String getMasterItemTag() {
        return masterItemTag;
    }

    public void setMasterItemTag(String masterItemTag) {
        this.masterItemTag = masterItemTag;
    }

    public Long getMasterItemId() {
        return masterItemId;
    }

    public void setMasterItemId(Long masterItemId) {
        this.masterItemId = masterItemId;
    }

    public String getThemeMasterImg() {
        return themeMasterImg;
    }

    public void setThemeMasterImg(String themeMasterImg) {
        this.themeMasterImg = themeMasterImg;
    }

    public Integer getThemeNum() {
        return themeNum;
    }

    public void setThemeNum(Integer themeNum) {
        this.themeNum = themeNum;
    }

    public String getH5Link() {
        return h5Link;
    }

    public void setH5Link(String h5Link) {
        this.h5Link = h5Link;
    }

    public Integer getThemeState() {
        return themeState;
    }

    public void setThemeState(Integer themeState) {
        this.themeState = themeState;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Integer getOffset() {
        return offset;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }

    public Integer getThemeCateCode() {
        return themeCateCode;
    }

    public void setThemeCateCode(Integer themeCateCode) {
        this.themeCateCode = themeCateCode;
    }

    @Override
    public String toString() {
        return "Theme{" +
                "id=" + id +
                ", themeImg='" + themeImg + '\'' +
                ", themeUrl='" + themeUrl + '\'' +
                ", type='" + type + '\'' +
                ", title='" + title + '\'' +
                ", startAt=" + startAt +
                ", endAt=" + endAt +
                ", sortNu=" + sortNu +
                ", orDestroy=" + orDestroy +
                ", destroyAt=" + destroyAt +
                ", updateAt=" + updateAt +
                ", createAt=" + createAt +
                ", themeSrcImg='" + themeSrcImg + '\'' +
                ", themeConfigInfo='" + themeConfigInfo + '\'' +
                ", themeItem='" + themeItem + '\'' +
                ", masterItemTag='" + masterItemTag + '\'' +
                ", masterItemId=" + masterItemId +
                ", themeMasterImg='" + themeMasterImg + '\'' +
                ", themeNum=" + themeNum +
                ", h5Link='" + h5Link + '\'' +
                ", themeState=" + themeState +
                ", pageSize=" + pageSize +
                ", offset=" + offset +
                ", themeCateCode=" + themeCateCode +
                '}';
    }
}
