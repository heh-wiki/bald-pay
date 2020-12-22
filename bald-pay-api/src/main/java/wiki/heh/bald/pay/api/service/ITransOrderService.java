package wiki.heh.bald.pay.api.service;

import com.alibaba.fastjson.JSONObject;

import java.util.Map;

/**
 * 转账业务
 *
 * @author heh
 * @version v1.0
 * @date 2020-12-21
 */
public interface ITransOrderService {

    int create(JSONObject transOrder);

    Map select(String jsonParam);

    Map selectByMchIdAndTransOrderId(String jsonParam);

    Map selectByMchIdAndMchTransNo(String jsonParam);

    Map updateStatus4Ing(String jsonParam);

    Map updateStatus4Success(String jsonParam);

    Map updateStatus4Complete(String jsonParam);

    Map sendTransNotify(String transOrderId, String channelName) ;

}
