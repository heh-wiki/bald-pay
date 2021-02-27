package wiki.heh.bald.pay.mgr.model.form;

/**
 * 商户信息参数类
 *
 * @author heh
 * @date 2021/2/5
 */
public class MchInfoForm {

    /*商户ID*/
    private String mchId;
    /*名称*/
    private String name;
    /*类型*/
    private String type;
    /*请求私钥*/
    private String reqKey;
    /*响应私钥*/
    private String resKey;
    /* 商户状态,0-禁用,1-启用*/
    private Byte state;

    public String getMchId() {
        return mchId;
    }

    public void setMchId(String mchId) {
        this.mchId = mchId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getReqKey() {
        return reqKey;
    }

    public void setReqKey(String reqKey) {
        this.reqKey = reqKey;
    }

    public String getResKey() {
        return resKey;
    }

    public void setResKey(String resKey) {
        this.resKey = resKey;
    }

    public Byte getState() {
        return state;
    }

    public void setState(Byte state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return "MchInfoParam{" +
                "mchId='" + mchId + '\'' +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", reqKey='" + reqKey + '\'' +
                ", resKey='" + resKey + '\'' +
                ", state=" + state +
                '}';
    }
}
