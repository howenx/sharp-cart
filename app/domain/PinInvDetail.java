package domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import util.DiscountSerializer;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

/**
 * 拼购库存商品详情页
 * Created by howen on 16/1/25.
 */
public class PinInvDetail {

    private Long id;         //商品库存ID
    private String shareUrl;    //分享短连接
    private String status;      //状态
    private String pinTitle;    //拼购商品标题
    @JsonFormat(shape= JsonFormat.Shape.STRING, pattern="yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Timestamp startAt;  //开始时间
    @JsonFormat(shape= JsonFormat.Shape.STRING, pattern="yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Timestamp endAt;        //结束时间
    @JsonIgnore
    private String pinPriceRule;    //价格阶梯
    private int restrictAmount;     //每个ID限购数量
    private String floorPrice;  //拼购最低价
    @JsonSerialize(using = DiscountSerializer.class)
    private BigDecimal pinDiscount; //拼购最低折扣

    private List<PinTieredPrice> pinTieredPrices;       //拼购商品价格及优惠信息

    private String pinRedirectUrl;//拼购跳转链接

    //inv
    private     String              invArea;//库存区域区分：'B'保税区仓库发货，‘Z’韩国直邮
    private     Integer             restAmount;//商品余量
    private     String              itemPreviewImgs;//sku预览图
    private     String              invWeight;//商品重量单位g
    private     String              invCustoms;//报关单位
    private     String              postalTaxRate;//税率,百分比
    private     String              postalStandard;//关税收费标准
    private     String              invAreaNm;//仓储地名称
    private     Integer             collectCount;//收藏数
    private     Integer             browseCount;//浏览次数
    private     Integer             soldAmount;//卖出数量
    private     String              invImg;//sku主图
    private     BigDecimal          invPrice;//商品原有价格
    private     String              skuType;//商品类型
    private     Long                skuTypeId;//商品类型所对应的ID

    //item
    @JsonIgnore
    private String itemDetailImgs;//商品详细图片
    @JsonIgnore
    private String itemFeatures;//商品参数
    @JsonIgnore
    private String publicity;//优惠区域显示信息（包括发货区域，多久后发货)
    @JsonIgnore
    private String detail;//商品详情

    public PinInvDetail() {
    }

    public PinInvDetail(Long id, String shareUrl, String status, String pinTitle, Timestamp startAt, Timestamp endAt, String pinPriceRule, int restrictAmount, String floorPrice, BigDecimal pinDiscount, List<PinTieredPrice> pinTieredPrices, String pinRedirectUrl, String invArea, Integer restAmount, String itemPreviewImgs, String invWeight, String invCustoms, String postalTaxRate, String postalStandard, String invAreaNm, Integer collectCount, Integer browseCount, Integer soldAmount, String invImg, BigDecimal invPrice, String skuType, Long skuTypeId, String itemDetailImgs, String itemFeatures, String publicity, String detail) {
        this.id = id;
        this.shareUrl = shareUrl;
        this.status = status;
        this.pinTitle = pinTitle;
        this.startAt = startAt;
        this.endAt = endAt;
        this.pinPriceRule = pinPriceRule;
        this.restrictAmount = restrictAmount;
        this.floorPrice = floorPrice;
        this.pinDiscount = pinDiscount;
        this.pinTieredPrices = pinTieredPrices;
        this.pinRedirectUrl = pinRedirectUrl;
        this.invArea = invArea;
        this.restAmount = restAmount;
        this.itemPreviewImgs = itemPreviewImgs;
        this.invWeight = invWeight;
        this.invCustoms = invCustoms;
        this.postalTaxRate = postalTaxRate;
        this.postalStandard = postalStandard;
        this.invAreaNm = invAreaNm;
        this.collectCount = collectCount;
        this.browseCount = browseCount;
        this.soldAmount = soldAmount;
        this.invImg = invImg;
        this.invPrice = invPrice;
        this.skuType = skuType;
        this.skuTypeId = skuTypeId;
        this.itemDetailImgs = itemDetailImgs;
        this.itemFeatures = itemFeatures;
        this.publicity = publicity;
        this.detail = detail;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getShareUrl() {
        return shareUrl;
    }

    public void setShareUrl(String shareUrl) {
        this.shareUrl = shareUrl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPinTitle() {
        return pinTitle;
    }

    public void setPinTitle(String pinTitle) {
        this.pinTitle = pinTitle;
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

    public String getPinPriceRule() {
        return pinPriceRule;
    }

    public void setPinPriceRule(String pinPriceRule) {
        this.pinPriceRule = pinPriceRule;
    }

    public int getRestrictAmount() {
        return restrictAmount;
    }

    public void setRestrictAmount(int restrictAmount) {
        this.restrictAmount = restrictAmount;
    }

    public String getFloorPrice() {
        return floorPrice;
    }

    public void setFloorPrice(String floorPrice) {
        this.floorPrice = floorPrice;
    }

    public BigDecimal getPinDiscount() {
        return pinDiscount;
    }

    public void setPinDiscount(BigDecimal pinDiscount) {
        this.pinDiscount = pinDiscount;
    }

    public List<PinTieredPrice> getPinTieredPrices() {
        return pinTieredPrices;
    }

    public void setPinTieredPrices(List<PinTieredPrice> pinTieredPrices) {
        this.pinTieredPrices = pinTieredPrices;
    }

    public String getPinRedirectUrl() {
        return pinRedirectUrl;
    }

    public void setPinRedirectUrl(String pinRedirectUrl) {
        this.pinRedirectUrl = pinRedirectUrl;
    }

    public String getInvArea() {
        return invArea;
    }

    public void setInvArea(String invArea) {
        this.invArea = invArea;
    }

    public Integer getRestAmount() {
        return restAmount;
    }

    public void setRestAmount(Integer restAmount) {
        this.restAmount = restAmount;
    }

    public String getItemPreviewImgs() {
        return itemPreviewImgs;
    }

    public void setItemPreviewImgs(String itemPreviewImgs) {
        this.itemPreviewImgs = itemPreviewImgs;
    }

    public String getInvWeight() {
        return invWeight;
    }

    public void setInvWeight(String invWeight) {
        this.invWeight = invWeight;
    }

    public String getInvCustoms() {
        return invCustoms;
    }

    public void setInvCustoms(String invCustoms) {
        this.invCustoms = invCustoms;
    }

    public String getPostalTaxRate() {
        return postalTaxRate;
    }

    public void setPostalTaxRate(String postalTaxRate) {
        this.postalTaxRate = postalTaxRate;
    }

    public String getPostalStandard() {
        return postalStandard;
    }

    public void setPostalStandard(String postalStandard) {
        this.postalStandard = postalStandard;
    }

    public String getInvAreaNm() {
        return invAreaNm;
    }

    public void setInvAreaNm(String invAreaNm) {
        this.invAreaNm = invAreaNm;
    }

    public Integer getCollectCount() {
        return collectCount;
    }

    public void setCollectCount(Integer collectCount) {
        this.collectCount = collectCount;
    }

    public Integer getBrowseCount() {
        return browseCount;
    }

    public void setBrowseCount(Integer browseCount) {
        this.browseCount = browseCount;
    }

    public Integer getSoldAmount() {
        return soldAmount;
    }

    public void setSoldAmount(Integer soldAmount) {
        this.soldAmount = soldAmount;
    }

    public String getInvImg() {
        return invImg;
    }

    public void setInvImg(String invImg) {
        this.invImg = invImg;
    }

    public BigDecimal getInvPrice() {
        return invPrice;
    }

    public void setInvPrice(BigDecimal invPrice) {
        this.invPrice = invPrice;
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

    public String getPublicity() {
        return publicity;
    }

    public void setPublicity(String publicity) {
        this.publicity = publicity;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    @Override
    public String toString() {
        return "PinInvDetail{" +
                "id=" + id +
                ", shareUrl='" + shareUrl + '\'' +
                ", status='" + status + '\'' +
                ", pinTitle='" + pinTitle + '\'' +
                ", startAt=" + startAt +
                ", endAt=" + endAt +
                ", pinPriceRule='" + pinPriceRule + '\'' +
                ", restrictAmount=" + restrictAmount +
                ", floorPrice='" + floorPrice + '\'' +
                ", pinDiscount=" + pinDiscount +
                ", pinTieredPrices=" + pinTieredPrices +
                ", pinRedirectUrl='" + pinRedirectUrl + '\'' +
                ", invArea='" + invArea + '\'' +
                ", restAmount=" + restAmount +
                ", itemPreviewImgs='" + itemPreviewImgs + '\'' +
                ", invWeight='" + invWeight + '\'' +
                ", invCustoms='" + invCustoms + '\'' +
                ", postalTaxRate='" + postalTaxRate + '\'' +
                ", postalStandard='" + postalStandard + '\'' +
                ", invAreaNm='" + invAreaNm + '\'' +
                ", collectCount=" + collectCount +
                ", browseCount=" + browseCount +
                ", soldAmount=" + soldAmount +
                ", invImg='" + invImg + '\'' +
                ", invPrice=" + invPrice +
                ", skuType='" + skuType + '\'' +
                ", skuTypeId=" + skuTypeId +
                ", itemDetailImgs='" + itemDetailImgs + '\'' +
                ", itemFeatures='" + itemFeatures + '\'' +
                ", publicity='" + publicity + '\'' +
                ", detail='" + detail + '\'' +
                '}';
    }
}
