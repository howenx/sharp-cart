package domain;

/**
 * 第三方登录表
 * Created by sibyl.sun on 16/5/18.
 */
public class IdThree {
    private Long id;             //主键
    private Integer userId;     //用户ID
    private String openId;      //第三方唯一用户识别ID
    private String idType;      //第三方平台，W：微信，Q:腾讯，A:阿里，WO:微信开放平台
    private String unionId;     //全局ID

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getOpenId() {
        return openId;
    }

    public void setOpenId(String openId) {
        this.openId = openId;
    }

    public String getIdType() {
        return idType;
    }

    public void setIdType(String idType) {
        this.idType = idType;
    }

    public String getUnionId() {
        return unionId;
    }

    public void setUnionId(String unionId) {
        this.unionId = unionId;
    }

    @Override
    public String toString(){
        return "IdThree{" +
                "id=" + id +
                ", userId=" + userId +
                ", openId='" + openId + '\'' +
                ", idType=" + idType +
                ", unionId=" + unionId +
                '}';
    }


}
