package domain;

import java.io.Serializable;

/**
 * Created by sibyl.sun on 16/8/30.
 */
public class Cates implements Serializable {
    /**
     * * cateId primary key.
     */
    private Long cateId;

    /**
     * parent cateId.
     */
    private Long pcateId;

    /**
     * cate  name.
     */
    private String cateNm;

    /**
     * cate description.
     */
    private String cateDesc;

    /**
     * cate cateCode.
     */
    private String cateCode;

    public Long getCateId() {
        return cateId;
    }

    public void setCateId(Long cateId) {
        this.cateId = cateId;
    }

    public Long getPcateId() {
        return pcateId;
    }

    public void setPcateId(Long pcateId) {
        this.pcateId = pcateId;
    }

    public String getCateNm() {
        return cateNm;
    }

    public void setCateNm(String cateNm) {
        this.cateNm = cateNm;
    }

    public String getCateDesc() {
        return cateDesc;
    }

    public void setCateDesc(String cateDesc) {
        this.cateDesc = cateDesc;
    }

    public String getCateCode() {
        return cateCode;
    }

    public void setCateCode(String cateCode) {
        this.cateCode = cateCode;
    }

    @Override
    public String toString() {
        return "Cates{" +
                "cateId=" + cateId +
                ", pcateId=" + pcateId +
                ", cateNm='" + cateNm + '\'' +
                ", cateDesc='" + cateDesc + '\'' +
                ", cateCode='" + cateCode + '\'' +
                '}';
    }
}
