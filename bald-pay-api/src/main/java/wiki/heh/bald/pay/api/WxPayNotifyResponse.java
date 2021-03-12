package wiki.heh.bald.pay.api;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.annotations.XStreamOmitField;
import me.chanjar.weixin.common.util.xml.XStreamCDataConverter;
import me.chanjar.weixin.common.util.xml.XStreamInitializer;

/**
 * @author heh
 * @date 2021/1/8
 */

@XStreamAlias("xml")
public class WxPayNotifyResponse {
    @XStreamOmitField
    private static final transient String FAIL = "FAIL";
    @XStreamOmitField
    private static final transient String SUCCESS = "SUCCESS";
    @XStreamAlias("return_code")
    @XStreamConverter(XStreamCDataConverter.class)
    private String returnCode;
    @XStreamConverter(XStreamCDataConverter.class)
    @XStreamAlias("return_msg")
    private String returnMsg;

    public WxPayNotifyResponse() {
    }

    public WxPayNotifyResponse(String returnCode, String returnMsg) {
        this.returnCode = returnCode;
        this.returnMsg = returnMsg;
    }

    public static String fail(String msg) {
        com.github.binarywang.wxpay.bean.notify.WxPayNotifyResponse response = new com.github.binarywang.wxpay.bean.notify.WxPayNotifyResponse("FAIL", msg);
        XStream xstream = XStreamInitializer.getInstance();
        xstream.autodetectAnnotations(true);
        return xstream.toXML(response);
    }

    public static String success(String msg) {
        com.github.binarywang.wxpay.bean.notify.WxPayNotifyResponse response = new com.github.binarywang.wxpay.bean.notify.WxPayNotifyResponse("SUCCESS", msg);
        XStream xstream = XStreamInitializer.getInstance();
        xstream.autodetectAnnotations(true);
        return xstream.toXML(response);
    }

    public String getReturnCode() {
        return this.returnCode;
    }

    public void setReturnCode(String returnCode) {
        this.returnCode = returnCode;
    }

    public String getReturnMsg() {
        return this.returnMsg;
    }

    public void setReturnMsg(String returnMsg) {
        this.returnMsg = returnMsg;
    }
}