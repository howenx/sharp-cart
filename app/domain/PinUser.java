package domain;

import java.io.Serializable;

/**
 * 拼购用户
 * Created by tiffany on 16/1/20.
 */
public class PinUser implements Serializable {
    private Long id;            //主键ID
    private Long userId;        //用户ID
    private boolean orMaster;   //是否团长
    private Long pinActiveId;   //拼购活动ID
    private String userIp;      //用户参与活动时IP
    private boolean orRobot;    //是否机器人
    private String userImg;     //用户头像

    public PinUser() {
    }

    public PinUser(Long id, Long userId, boolean orMaster, Long pinActiveId, String userIp, boolean orRobot, String userImg) {
        this.id = id;
        this.userId = userId;
        this.orMaster = orMaster;
        this.pinActiveId = pinActiveId;
        this.userIp = userIp;
        this.orRobot = orRobot;
        this.userImg = userImg;
    }

    @Override
    public String toString() {
        return "PinUser{" +
                "id=" + id +
                ", userId=" + userId +
                ", orMaster=" + orMaster +
                ", pinActiveId=" + pinActiveId +
                ", userIp='" + userIp + '\'' +
                ", orRobot=" + orRobot +
                ", userImg='" + userImg + '\'' +
                '}';
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public boolean isOrMaster() {
        return orMaster;
    }

    public void setOrMaster(boolean orMaster) {
        this.orMaster = orMaster;
    }

    public Long getPinActiveId() {
        return pinActiveId;
    }

    public void setPinActiveId(Long pinActiveId) {
        this.pinActiveId = pinActiveId;
    }

    public String getUserIp() {
        return userIp;
    }

    public void setUserIp(String userIp) {
        this.userIp = userIp;
    }

    public boolean isOrRobot() {
        return orRobot;
    }

    public void setOrRobot(boolean orRobot) {
        this.orRobot = orRobot;
    }

    public String getUserImg() {
        return userImg;
    }

    public void setUserImg(String userImg) {
        this.userImg = userImg;
    }
}
