package domain;

import java.io.Serializable;
import java.util.List;

/**
 * 用于购物车方法间参数传递
 * Created by howen on 16/2/22.
 */
public class CartPar implements Serializable{

    Long sCartIds;//失效商品购物车

    Integer restrictMessageCode;//消息码

    Integer restMessageCode;

    public CartPar() {
    }

    public CartPar(Long sCartIds, Integer restrictMessageCode, Integer restMessageCode) {
        this.sCartIds = sCartIds;
        this.restrictMessageCode = restrictMessageCode;
        this.restMessageCode = restMessageCode;
    }

    public Long getsCartIds() {
        return sCartIds;
    }

    public void setsCartIds(Long sCartIds) {
        this.sCartIds = sCartIds;
    }

    public Integer getRestrictMessageCode() {
        return restrictMessageCode;
    }

    public void setRestrictMessageCode(Integer restrictMessageCode) {
        this.restrictMessageCode = restrictMessageCode;
    }

    public Integer getRestMessageCode() {
        return restMessageCode;
    }

    public void setRestMessageCode(Integer restMessageCode) {
        this.restMessageCode = restMessageCode;
    }

    @Override
    public String toString() {
        return "CartPar{" +
                "sCartIds=" + sCartIds +
                ", restrictMessageCode=" + restrictMessageCode +
                ", restMessageCode=" + restMessageCode +
                '}';
    }
}
