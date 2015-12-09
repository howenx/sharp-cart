package domain;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 邮费模版
 * Created by howen on 15/12/9.
 */
public class Carriage implements Serializable {

    private Long id;
    private Integer firstNum;
    private BigDecimal firstFee;
    private Integer addNum;
    private BigDecimal addFee;
    private String modelName;
    private String cityCode;
    private String modelCode;

    public Carriage() {
    }

    public Carriage(Long id, Integer firstNum, BigDecimal firstFee, Integer addNum, BigDecimal addFee, String modelName, String cityCode, String modelCode) {
        this.id = id;
        this.firstNum = firstNum;
        this.firstFee = firstFee;
        this.addNum = addNum;
        this.addFee = addFee;
        this.modelName = modelName;
        this.cityCode = cityCode;
        this.modelCode = modelCode;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getFirstNum() {
        return firstNum;
    }

    public void setFirstNum(Integer firstNum) {
        this.firstNum = firstNum;
    }

    public BigDecimal getFirstFee() {
        return firstFee;
    }

    public void setFirstFee(BigDecimal firstFee) {
        this.firstFee = firstFee;
    }

    public Integer getAddNum() {
        return addNum;
    }

    public void setAddNum(Integer addNum) {
        this.addNum = addNum;
    }

    public BigDecimal getAddFee() {
        return addFee;
    }

    public void setAddFee(BigDecimal addFee) {
        this.addFee = addFee;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getCityCode() {
        return cityCode;
    }

    public void setCityCode(String cityCode) {
        this.cityCode = cityCode;
    }

    public String getModelCode() {
        return modelCode;
    }

    public void setModelCode(String modelCode) {
        this.modelCode = modelCode;
    }

    @Override
    public String toString() {
        return "Carriage{" +
                "id=" + id +
                ", firstNum=" + firstNum +
                ", firstFee=" + firstFee +
                ", addNum=" + addNum +
                ", addFee=" + addFee +
                ", modelName='" + modelName + '\'' +
                ", cityCode='" + cityCode + '\'' +
                ", modelCode='" + modelCode + '\'' +
                '}';
    }
}