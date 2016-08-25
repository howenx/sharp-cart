package domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * 商品vo
 * Created by howen on 15/11/12.
 */
public class Item implements Serializable {
    private static final long serialVersionUID = 21L;
    private Long id;//商品ID
    private String itemTitle;//商品标题
    private String itemDetailImgs;//商品详细图片
    private String itemFeatures;//商品参数
    private String itemNotice;//商品重要布告
    private String publicity;//优惠区域显示信息（包括发货区域，多久后发货）

    @JsonIgnore
    private Long masterInvId;//主SKU的ID
    @JsonIgnore
    private String itemMasterImg;//商品主图,用于列表页显示的图片
    @JsonIgnore
    private String onShelvesAt;//上架时间
    @JsonIgnore
    private String offShelvesAt;//下架时间
    @JsonIgnore
    private Long themeId;//商品属于主题的ID
    @JsonIgnore
    private String state;//商品状态 'Y'--正常,'D'--下架,'N'--删除,'K'--售空
    @JsonIgnore
    private String shareUrl;//分享链接
    @JsonIgnore
    private Integer collectCount;//收藏数
    @JsonIgnore
    private Long cateId;
    @JsonIgnore
    private Long brandId;
    @JsonIgnore
    private String supplyMerch;
    @JsonIgnore
    private Long shareCount;
    @JsonIgnore
    private Long browseCount;
    @JsonIgnore
    private Boolean orDestroy;
    @JsonIgnore
    private Timestamp destroyAt;
    @JsonIgnore
    private Timestamp updateAt;
    @JsonIgnore
    private Timestamp createAt;
    @JsonIgnore
    private String itemDetail;

    public Item() {
    }

    public Item(Long id, String itemTitle, String itemDetailImgs, String itemFeatures, String itemNotice, String publicity, Long masterInvId, String itemMasterImg, String onShelvesAt, String offShelvesAt, Long themeId, String state, String shareUrl, Integer collectCount, Long cateId, Long brandId, String supplyMerch, Long shareCount, Long browseCount, Boolean orDestroy, Timestamp destroyAt, Timestamp updateAt, Timestamp createAt, String itemDetail) {
        this.id = id;
        this.itemTitle = itemTitle;
        this.itemDetailImgs = itemDetailImgs;
        this.itemFeatures = itemFeatures;
        this.itemNotice = itemNotice;
        this.publicity = publicity;
        this.masterInvId = masterInvId;
        this.itemMasterImg = itemMasterImg;
        this.onShelvesAt = onShelvesAt;
        this.offShelvesAt = offShelvesAt;
        this.themeId = themeId;
        this.state = state;
        this.shareUrl = shareUrl;
        this.collectCount = collectCount;
        this.cateId = cateId;
        this.brandId = brandId;
        this.supplyMerch = supplyMerch;
        this.shareCount = shareCount;
        this.browseCount = browseCount;
        this.orDestroy = orDestroy;
        this.destroyAt = destroyAt;
        this.updateAt = updateAt;
        this.createAt = createAt;
        this.itemDetail = itemDetail;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getItemTitle() {
        return itemTitle;
    }

    public void setItemTitle(String itemTitle) {
        this.itemTitle = itemTitle;
    }

    public String getItemDetailImgs() {
        return itemDetailImgs;
    }

    public void setItemDetailImgs(String itemDetailImgs) {
        this.itemDetailImgs = itemDetailImgs;
    }

    public String getItemFeatures() {
        return itemFeatures;
    }

    public void setItemFeatures(String itemFeatures) {
        this.itemFeatures = itemFeatures;
    }

    public String getItemNotice() {
        return itemNotice;
    }

    public void setItemNotice(String itemNotice) {
        this.itemNotice = itemNotice;
    }

    public String getPublicity() {
        return publicity;
    }

    public void setPublicity(String publicity) {
        this.publicity = publicity;
    }

    public Long getMasterInvId() {
        return masterInvId;
    }

    public void setMasterInvId(Long masterInvId) {
        this.masterInvId = masterInvId;
    }

    public String getItemMasterImg() {
        return itemMasterImg;
    }

    public void setItemMasterImg(String itemMasterImg) {
        this.itemMasterImg = itemMasterImg;
    }

    public String getOnShelvesAt() {
        return onShelvesAt;
    }

    public void setOnShelvesAt(String onShelvesAt) {
        this.onShelvesAt = onShelvesAt;
    }

    public String getOffShelvesAt() {
        return offShelvesAt;
    }

    public void setOffShelvesAt(String offShelvesAt) {
        this.offShelvesAt = offShelvesAt;
    }

    public Long getThemeId() {
        return themeId;
    }

    public void setThemeId(Long themeId) {
        this.themeId = themeId;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getShareUrl() {
        return shareUrl;
    }

    public void setShareUrl(String shareUrl) {
        this.shareUrl = shareUrl;
    }

    public Integer getCollectCount() {
        return collectCount;
    }

    public void setCollectCount(Integer collectCount) {
        this.collectCount = collectCount;
    }

    public Long getCateId() {
        return cateId;
    }

    public void setCateId(Long cateId) {
        this.cateId = cateId;
    }

    public Long getBrandId() {
        return brandId;
    }

    public void setBrandId(Long brandId) {
        this.brandId = brandId;
    }

    public String getSupplyMerch() {
        return supplyMerch;
    }

    public void setSupplyMerch(String supplyMerch) {
        this.supplyMerch = supplyMerch;
    }

    public Long getShareCount() {
        return shareCount;
    }

    public void setShareCount(Long shareCount) {
        this.shareCount = shareCount;
    }

    public Long getBrowseCount() {
        return browseCount;
    }

    public void setBrowseCount(Long browseCount) {
        this.browseCount = browseCount;
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

    public String getItemDetail() {
        return itemDetail;
    }

    public void setItemDetail(String itemDetail) {
        this.itemDetail = itemDetail;
    }

    @Override
    public String toString() {
        return "Item{" +
                "id=" + id +
                ", itemTitle='" + itemTitle + '\'' +
                ", itemDetailImgs='" + itemDetailImgs + '\'' +
                ", itemFeatures='" + itemFeatures + '\'' +
                ", itemNotice='" + itemNotice + '\'' +
                ", publicity='" + publicity + '\'' +
                ", masterInvId=" + masterInvId +
                ", itemMasterImg='" + itemMasterImg + '\'' +
                ", onShelvesAt='" + onShelvesAt + '\'' +
                ", offShelvesAt='" + offShelvesAt + '\'' +
                ", themeId=" + themeId +
                ", state='" + state + '\'' +
                ", shareUrl='" + shareUrl + '\'' +
                ", collectCount=" + collectCount +
                ", cateId=" + cateId +
                ", brandId=" + brandId +
                ", supplyMerch='" + supplyMerch + '\'' +
                ", shareCount=" + shareCount +
                ", browseCount=" + browseCount +
                ", orDestroy=" + orDestroy +
                ", destroyAt=" + destroyAt +
                ", updateAt=" + updateAt +
                ", createAt=" + createAt +
                ", itemDetail='" + itemDetail + '\'' +
                '}';
    }
}