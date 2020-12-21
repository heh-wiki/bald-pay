package wiki.heh.bald.pay.api.service;

import java.util.Map;


/**
 * 退款业务
 *
 * @author heh
 * @version v1.0
 * @date 2020-12-18
 */
public interface IRefundOrderService {

    Map create(String jsonParam);

    Map select(String jsonParam);

    Map selectByMchIdAndRefundOrderId(String jsonParam);

    Map selectByMchIdAndMchRefundNo(String jsonParam);

    Map updateStatus4Ing(String jsonParam);

    Map updateStatus4Success(String jsonParam);

    Map updateStatus4Complete(String jsonParam);

    Map sendRefundNotify(String jsonParam);

}
