package domain;

import java.io.Serializable;

/**
 * 系统参数VO
 * Created by howen on 15/12/8.
 */
public class SysParameter implements Serializable {

    private Long sysId           ;
    private String parameterNm     ;
    private String parameterVal    ;
    private String parameterCode;

    public SysParameter(Long sysId, String parameterNm, String parameterVal, String parameterCode) {
        this.sysId = sysId;
        this.parameterNm = parameterNm;
        this.parameterVal = parameterVal;
        this.parameterCode = parameterCode;
    }

    public SysParameter() {
    }

    public Long getSysId() {
        return sysId;
    }

    public void setSysId(Long sysId) {
        this.sysId = sysId;
    }

    public String getParameterNm() {
        return parameterNm;
    }

    public void setParameterNm(String parameterNm) {
        this.parameterNm = parameterNm;
    }

    public String getParameterVal() {
        return parameterVal;
    }

    public void setParameterVal(String parameterVal) {
        this.parameterVal = parameterVal;
    }

    public String getParameterCode() {
        return parameterCode;
    }

    public void setParameterCode(String parameterCode) {
        this.parameterCode = parameterCode;
    }

    @Override
    public String toString() {
        return "SysParameter{" +
                "sysId=" + sysId +
                ", parameterNm='" + parameterNm + '\'' +
                ", parameterVal='" + parameterVal + '\'' +
                ", parameterCode='" + parameterCode + '\'' +
                '}';
    }
}
