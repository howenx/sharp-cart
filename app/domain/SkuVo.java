package domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import util.DiscountSerializer;
import util.MoneySerializer;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * 视图;用于查询所有商品的
 * Created by howen on 16/2/26.
 */
public class SkuVo implements Serializable{

    private String skuType;//商品类型
    private Long skuTypeId;//商品类型ID
    private Long invId;//库存ID
    private String skuTypeStatus;//商品状态
    @JsonSerialize(using = MoneySerializer.class)
    private BigDecimal skuTypePrice;//商品售价
    @JsonIgnore
    private String  skuTypeThemeId;//商品已经加入的主题
    private Integer skuTypeSoldAmount;//商品销售数量
    @JsonIgnore
    private Integer skuTypeLimitAmount;//商品限制卖出总数量
    private String skuTypeTitle;//商品标题
    private Timestamp skuTypeStartAt;//商品销售开始时间
    private Timestamp skuTypeEndAt;//商品下架时间
    private Integer skuTypeRestrictAmount;//商品限制用户购买数量
    private String skuTypeFloorPrice;//用于拼购的,显示最低价格和团购人数
    @JsonSerialize(using = DiscountSerializer.class)
    private BigDecimal skuTypeDiscount;//商品折扣
    private String skuTypeImg;//商品主图,单独的一张,主要用于在主题列表中显示
    @JsonIgnore
    private Long id;//库存ID
    private Long itemId;//库存共享详情页的itemId
    private String itemColor;//商品颜色
    private String itemSize;//商品尺寸
    private Integer amount;//商品数量
    @JsonSerialize(using = MoneySerializer.class)
    private BigDecimal itemSrcPrice;//用于计算折扣的显示的原价
    @JsonIgnore
    @JsonSerialize(using = MoneySerializer.class)
    private BigDecimal itemPrice;//商品在售价格
    @JsonIgnore
    @JsonSerialize(using = MoneySerializer.class)
    private BigDecimal itemCostPrice;//商品成本价
    @JsonIgnore
    @JsonSerialize(using = DiscountSerializer.class)
    private BigDecimal itemDiscount;//商品折扣
    @JsonIgnore
    private Integer soldAmount;//卖出数量
    private Integer restAmount;//剩余数量
    private String invImg;//商品主图
    private String itemPreviewImgs;//商品预览图
    @JsonIgnore
    private Boolean orDestroy;//是否删除
    @JsonIgnore
    private Timestamp destroyAt;//删除时间
    @JsonIgnore
    private Timestamp updateAt;//更新时间
    @JsonIgnore
    private Timestamp createAt;//创建时间

    private Boolean orMasterInv;//是否主SKu
    @JsonIgnore
    private String state;//商品状态
    private String invArea;//库存区域区分
    @JsonIgnore
    private Long restrictAmount;//针对用户的限购数量
    @JsonIgnore
    private String invTitle;//商品标题
    private String invCustoms;//商品报关单位
    @JsonIgnore
    private String postalTaxCode;//行邮税编号
    @JsonIgnore
    private Integer invWeight;//商品毛重
    private String postalTaxRate;//行邮税税率
    @JsonIgnore
    private String carriageModelCode;//邮费模版code
    @JsonIgnore
    private String recordCode;//备案号
    @JsonIgnore
    private Timestamp startAt;//开始时间
    @JsonIgnore
    private Timestamp endAt;//结束时间
    @JsonIgnore
    private Boolean orVaryPrice;//是否存在多样化价格
    @JsonIgnore
    private String shareUrl;//分享链接
    @JsonIgnore
    private Long shareCount;//分享数量

    private Long collectCount;//收藏数量
    @JsonIgnore
    private Long browseCount;//浏览数量
    @JsonIgnore
    private String themeId;//主题ID
    @JsonIgnore
    private String invCode;//规格编号

    private String invAreaNm;//报关区域名
    private String postalStandard;//行邮税标准

    public SkuVo() {
    }

    public SkuVo(String skuType, Long skuTypeId, Long invId, String skuTypeStatus, BigDecimal skuTypePrice, String skuTypeThemeId, Integer skuTypeSoldAmount, Integer skuTypeLimitAmount, String skuTypeTitle, Timestamp skuTypeStartAt, Timestamp skuTypeEndAt, Integer skuTypeRestrictAmount, String skuTypeFloorPrice, BigDecimal skuTypeDiscount, String skuTypeImg, Long id, Long itemId, String itemColor, String itemSize, Integer amount, BigDecimal itemSrcPrice, BigDecimal itemPrice, BigDecimal itemCostPrice, BigDecimal itemDiscount, Integer soldAmount, Integer restAmount, String invImg, String itemPreviewImgs, Boolean orDestroy, Timestamp destroyAt, Timestamp updateAt, Timestamp createAt, Boolean orMasterInv, String state, String invArea, Long restrictAmount, String invTitle, String invCustoms, String postalTaxCode, Integer invWeight, String postalTaxRate, String carriageModelCode, String recordCode, Timestamp startAt, Timestamp endAt, Boolean orVaryPrice, String shareUrl, Long shareCount, Long collectCount, Long browseCount, String themeId, String invCode, String invAreaNm, String postalStandard) {
        this.skuType = skuType;
        this.skuTypeId = skuTypeId;
        this.invId = invId;
        this.skuTypeStatus = skuTypeStatus;
        this.skuTypePrice = skuTypePrice;
        this.skuTypeThemeId = skuTypeThemeId;
        this.skuTypeSoldAmount = skuTypeSoldAmount;
        this.skuTypeLimitAmount = skuTypeLimitAmount;
        this.skuTypeTitle = skuTypeTitle;
        this.skuTypeStartAt = skuTypeStartAt;
        this.skuTypeEndAt = skuTypeEndAt;
        this.skuTypeRestrictAmount = skuTypeRestrictAmount;
        this.skuTypeFloorPrice = skuTypeFloorPrice;
        this.skuTypeDiscount = skuTypeDiscount;
        this.skuTypeImg = skuTypeImg;
        this.id = id;
        this.itemId = itemId;
        this.itemColor = itemColor;
        this.itemSize = itemSize;
        this.amount = amount;
        this.itemSrcPrice = itemSrcPrice;
        this.itemPrice = itemPrice;
        this.itemCostPrice = itemCostPrice;
        this.itemDiscount = itemDiscount;
        this.soldAmount = soldAmount;
        this.restAmount = restAmount;
        this.invImg = invImg;
        this.itemPreviewImgs = itemPreviewImgs;
        this.orDestroy = orDestroy;
        this.destroyAt = destroyAt;
        this.updateAt = updateAt;
        this.createAt = createAt;
        this.orMasterInv = orMasterInv;
        this.state = state;
        this.invArea = invArea;
        this.restrictAmount = restrictAmount;
        this.invTitle = invTitle;
        this.invCustoms = invCustoms;
        this.postalTaxCode = postalTaxCode;
        this.invWeight = invWeight;
        this.postalTaxRate = postalTaxRate;
        this.carriageModelCode = carriageModelCode;
        this.recordCode = recordCode;
        this.startAt = startAt;
        this.endAt = endAt;
        this.orVaryPrice = orVaryPrice;
        this.shareUrl = shareUrl;
        this.shareCount = shareCount;
        this.collectCount = collectCount;
        this.browseCount = browseCount;
        this.themeId = themeId;
        this.invCode = invCode;
        this.invAreaNm = invAreaNm;
        this.postalStandard = postalStandard;
    }

    public String getSkuType() {
        return skuType;
    }

    public void setSkuType(String skuType) {
        this.skuType = skuType;
    }

    public Long getSkuTypeId() {
        return skuTypeId;
    }

    public void setSkuTypeId(Long skuTypeId) {
        this.skuTypeId = skuTypeId;
    }

    public Long getInvId() {
        return invId;
    }

    public void setInvId(Long invId) {
        this.invId = invId;
    }

    public String getSkuTypeStatus() {
        return skuTypeStatus;
    }

    public void setSkuTypeStatus(String skuTypeStatus) {
        this.skuTypeStatus = skuTypeStatus;
    }

    public BigDecimal getSkuTypePrice() {
        return skuTypePrice;
    }

    public void setSkuTypePrice(BigDecimal skuTypePrice) {
        this.skuTypePrice = skuTypePrice;
    }

    public String getSkuTypeThemeId() {
        return skuTypeThemeId;
    }

    public void setSkuTypeThemeId(String skuTypeThemeId) {
        this.skuTypeThemeId = skuTypeThemeId;
    }

    public Integer getSkuTypeSoldAmount() {
        return skuTypeSoldAmount;
    }

    public void setSkuTypeSoldAmount(Integer skuTypeSoldAmount) {
        this.skuTypeSoldAmount = skuTypeSoldAmount;
    }

    public Integer getSkuTypeLimitAmount() {
        return skuTypeLimitAmount;
    }

    public void setSkuTypeLimitAmount(Integer skuTypeLimitAmount) {
        this.skuTypeLimitAmount = skuTypeLimitAmount;
    }

    public String getSkuTypeTitle() {
        return skuTypeTitle;
    }

    public void setSkuTypeTitle(String skuTypeTitle) {
        this.skuTypeTitle = skuTypeTitle;
    }

    public Timestamp getSkuTypeStartAt() {
        return skuTypeStartAt;
    }

    public void setSkuTypeStartAt(Timestamp skuTypeStartAt) {
        this.skuTypeStartAt = skuTypeStartAt;
    }

    public Timestamp getSkuTypeEndAt() {
        return skuTypeEndAt;
    }

    public void setSkuTypeEndAt(Timestamp skuTypeEndAt) {
        this.skuTypeEndAt = skuTypeEndAt;
    }

    public Integer getSkuTypeRestrictAmount() {
        return skuTypeRestrictAmount;
    }

    public void setSkuTypeRestrictAmount(Integer skuTypeRestrictAmount) {
        this.skuTypeRestrictAmount = skuTypeRestrictAmount;
    }

    public String getSkuTypeFloorPrice() {
        return skuTypeFloorPrice;
    }

    public void setSkuTypeFloorPrice(String skuTypeFloorPrice) {
        this.skuTypeFloorPrice = skuTypeFloorPrice;
    }

    public BigDecimal getSkuTypeDiscount() {
        return skuTypeDiscount;
    }

    public void setSkuTypeDiscount(BigDecimal skuTypeDiscount) {
        this.skuTypeDiscount = skuTypeDiscount;
    }

    public String getSkuTypeImg() {
        return skuTypeImg;
    }

    public void setSkuTypeImg(String skuTypeImg) {
        this.skuTypeImg = skuTypeImg;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public String getItemColor() {
        return itemColor;
    }

    public void setItemColor(String itemColor) {
        this.itemColor = itemColor;
    }

    public String getItemSize() {
        return itemSize;
    }

    public void setItemSize(String itemSize) {
        this.itemSize = itemSize;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public BigDecimal getItemSrcPrice() {
        return itemSrcPrice;
    }

    public void setItemSrcPrice(BigDecimal itemSrcPrice) {
        this.itemSrcPrice = itemSrcPrice;
    }

    public BigDecimal getItemPrice() {
        return itemPrice;
    }

    public void setItemPrice(BigDecimal itemPrice) {
        this.itemPrice = itemPrice;
    }

    public BigDecimal getItemCostPrice() {
        return itemCostPrice;
    }

    public void setItemCostPrice(BigDecimal itemCostPrice) {
        this.itemCostPrice = itemCostPrice;
    }

    public BigDecimal getItemDiscount() {
        return itemDiscount;
    }

    public void setItemDiscount(BigDecimal itemDiscount) {
        this.itemDiscount = itemDiscount;
    }

    public Integer getSoldAmount() {
        return soldAmount;
    }

    public void setSoldAmount(Integer soldAmount) {
        this.soldAmount = soldAmount;
    }

    public Integer getRestAmount() {
        return restAmount;
    }

    public void setRestAmount(Integer restAmount) {
        this.restAmount = restAmount;
    }

    public String getInvImg() {
        return invImg;
    }

    public void setInvImg(String invImg) {
        this.invImg = invImg;
    }

    public String getItemPreviewImgs() {
        return itemPreviewImgs;
    }

    public void setItemPreviewImgs(String itemPreviewImgs) {
        this.itemPreviewImgs = itemPreviewImgs;
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

    public Boolean getOrMasterInv() {
        return orMasterInv;
    }

    public void setOrMasterInv(Boolean orMasterInv) {
        this.orMasterInv = orMasterInv;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getInvArea() {
        return invArea;
    }

    public void setInvArea(String invArea) {
        this.invArea = invArea;
    }

    public Long getRestrictAmount() {
        return restrictAmount;
    }

    public void setRestrictAmount(Long restrictAmount) {
        this.restrictAmount = restrictAmount;
    }

    public String getInvTitle() {
        return invTitle;
    }

    public void setInvTitle(String invTitle) {
        this.invTitle = invTitle;
    }

    public String getInvCustoms() {
        return invCustoms;
    }

    public void setInvCustoms(String invCustoms) {
        this.invCustoms = invCustoms;
    }

    public String getPostalTaxCode() {
        return postalTaxCode;
    }

    public void setPostalTaxCode(String postalTaxCode) {
        this.postalTaxCode = postalTaxCode;
    }

    public Integer getInvWeight() {
        return invWeight;
    }

    public void setInvWeight(Integer invWeight) {
        this.invWeight = invWeight;
    }

    public String getPostalTaxRate() {
        return postalTaxRate;
    }

    public void setPostalTaxRate(String postalTaxRate) {
        this.postalTaxRate = postalTaxRate;
    }

    public String getCarriageModelCode() {
        return carriageModelCode;
    }

    public void setCarriageModelCode(String carriageModelCode) {
        this.carriageModelCode = carriageModelCode;
    }

    public String getRecordCode() {
        return recordCode;
    }

    public void setRecordCode(String recordCode) {
        this.recordCode = recordCode;
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

    public Boolean getOrVaryPrice() {
        return orVaryPrice;
    }

    public void setOrVaryPrice(Boolean orVaryPrice) {
        this.orVaryPrice = orVaryPrice;
    }

    public String getShareUrl() {
        return shareUrl;
    }

    public void setShareUrl(String shareUrl) {
        this.shareUrl = shareUrl;
    }

    public Long getShareCount() {
        return shareCount;
    }

    public void setShareCount(Long shareCount) {
        this.shareCount = shareCount;
    }

    public Long getCollectCount() {
        return collectCount;
    }

    public void setCollectCount(Long collectCount) {
        this.collectCount = collectCount;
    }

    public Long getBrowseCount() {
        return browseCount;
    }

    public void setBrowseCount(Long browseCount) {
        this.browseCount = browseCount;
    }

    public String getThemeId() {
        return themeId;
    }

    public void setThemeId(String themeId) {
        this.themeId = themeId;
    }

    public String getInvCode() {
        return invCode;
    }

    public void setInvCode(String invCode) {
        this.invCode = invCode;
    }

    public String getInvAreaNm() {
        return invAreaNm;
    }

    public void setInvAreaNm(String invAreaNm) {
        this.invAreaNm = invAreaNm;
    }

    public String getPostalStandard() {
        return postalStandard;
    }

    public void setPostalStandard(String postalStandard) {
        this.postalStandard = postalStandard;
    }

    @Override
    public String toString() {
        return "SkuVo{" +
                "skuType='" + skuType + '\'' +
                ", skuTypeId=" + skuTypeId +
                ", invId=" + invId +
                ", skuTypeStatus='" + skuTypeStatus + '\'' +
                ", skuTypePrice=" + skuTypePrice +
                ", skuTypeThemeId='" + skuTypeThemeId + '\'' +
                ", skuTypeSoldAmount=" + skuTypeSoldAmount +
                ", skuTypeLimitAmount=" + skuTypeLimitAmount +
                ", skuTypeTitle='" + skuTypeTitle + '\'' +
                ", skuTypeStartAt=" + skuTypeStartAt +
                ", skuTypeEndAt=" + skuTypeEndAt +
                ", skuTypeRestrictAmount=" + skuTypeRestrictAmount +
                ", skuTypeFloorPrice='" + skuTypeFloorPrice + '\'' +
                ", skuTypeDiscount=" + skuTypeDiscount +
                ", skuTypeImg='" + skuTypeImg + '\'' +
                ", id=" + id +
                ", itemId=" + itemId +
                ", itemColor='" + itemColor + '\'' +
                ", itemSize='" + itemSize + '\'' +
                ", amount=" + amount +
                ", itemSrcPrice=" + itemSrcPrice +
                ", itemPrice=" + itemPrice +
                ", itemCostPrice=" + itemCostPrice +
                ", itemDiscount=" + itemDiscount +
                ", soldAmount=" + soldAmount +
                ", restAmount=" + restAmount +
                ", invImg='" + invImg + '\'' +
                ", itemPreviewImgs='" + itemPreviewImgs + '\'' +
                ", orDestroy=" + orDestroy +
                ", destroyAt=" + destroyAt +
                ", updateAt=" + updateAt +
                ", createAt=" + createAt +
                ", orMasterInv=" + orMasterInv +
                ", state='" + state + '\'' +
                ", invArea='" + invArea + '\'' +
                ", restrictAmount=" + restrictAmount +
                ", invTitle='" + invTitle + '\'' +
                ", invCustoms='" + invCustoms + '\'' +
                ", postalTaxCode='" + postalTaxCode + '\'' +
                ", invWeight=" + invWeight +
                ", postalTaxRate='" + postalTaxRate + '\'' +
                ", carriageModelCode='" + carriageModelCode + '\'' +
                ", recordCode='" + recordCode + '\'' +
                ", startAt=" + startAt +
                ", endAt=" + endAt +
                ", orVaryPrice=" + orVaryPrice +
                ", shareUrl='" + shareUrl + '\'' +
                ", shareCount=" + shareCount +
                ", collectCount=" + collectCount +
                ", browseCount=" + browseCount +
                ", themeId='" + themeId + '\'' +
                ", invCode='" + invCode + '\'' +
                ", invAreaNm='" + invAreaNm + '\'' +
                ", postalStandard='" + postalStandard + '\'' +
                '}';
    }
}
