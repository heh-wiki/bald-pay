package wiki.heh.bald.pay.service.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import wiki.heh.bald.pay.common.constant.PayConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wiki.heh.bald.pay.service.model.PayOrder;
import wiki.heh.bald.pay.service.model.form.CreatePayOrderForm;
import wiki.heh.bald.pay.service.model.form.PayQueryForm;
import wiki.heh.bald.pay.service.service.PayOrderService;

/**
 * 支付订单接口
 *
 * @author hehua
 * @version v1.0
 */
@Api(tags = "支付订单接口")
@RestController
public class PayOrderServiceController extends Notify4BasePay {
    private final Logger _log = LoggerFactory.getLogger(PayOrderServiceController.class);
    @Autowired
    private PayOrderService payOrderService;

    @ApiOperation("创建支付")
    @PostMapping("pay/create")
    public String createPayOrder(@RequestBody CreatePayOrderForm form) {
        _log.info("接收创建支付订单请求,form={}", form);
        JSONObject retObj = new JSONObject();
        try {
            PayOrder payOrder = new PayOrder();
            BeanUtils.copyProperties(form, payOrder);
            int result = payOrderService.createPayOrder(payOrder);
            retObj.put("result", result);
        } catch (Exception e) {
            retObj.put("code", "9999"); // 系统错误
            retObj.put("msg", "系统错误");
            e.printStackTrace();
        }
        return retObj.toJSONString();
    }

    @ApiOperation("支付查询")
    @GetMapping("pay/query")
    public String queryPayOrder(PayQueryForm form) {
        _log.info("PayQueryForm << {}", form);
        JSONObject retObj = new JSONObject();
        PayOrder payOrder;
        if (StringUtils.isNotBlank(form.getPayOrderId())) {
            payOrder = payOrderService.selectPayOrderByMchIdAndPayOrderId(form.getMchId(), form.getPayOrderId());
        } else {
            payOrder = payOrderService.selectPayOrderByMchIdAndMchOrderNo(form.getMchId(), form.getMchOrderNo());
        }
        if (payOrder == null) {
            retObj.put("code", "0002");
            retObj.put("msg", "支付订单不存在");
            return retObj.toJSONString();
        }
        // 如果选择回调且支付状态为支付成功,则回调业务系统
        if (form.getExecuteNotify() && payOrder.getStatus() == PayConstant.PAY_STATUS_SUCCESS) {
            this.doNotify(payOrder,false);
        }
        retObj.put("result", JSON.toJSON(payOrder));
        _log.info("selectPayOrder >> {}", retObj);
        return retObj.toJSONString();
    }

}
