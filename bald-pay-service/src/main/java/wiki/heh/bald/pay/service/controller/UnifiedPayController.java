package wiki.heh.bald.pay.service.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import wiki.heh.bald.pay.common.util.DateUtil;
import wiki.heh.bald.pay.service.model.form.CreatePayOrderForm;
import wiki.heh.bald.pay.service.model.form.PayForm;
import wiki.heh.bald.pay.service.model.form.TestUnifiedPayForm;
import wiki.heh.bald.pay.service.model.form.UnifiedPayForm;
import wiki.heh.bald.pay.service.model.vo.Result;
import wiki.heh.bald.pay.service.model.vo.UnifiedPayVo;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author heh
 * @date 2020/12/3
 */
@Api(tags = "统一接口")
@RestController
public class UnifiedPayController {

    @Autowired
    PayOrderServiceController orderServiceController;
    @Autowired
    PayChannel4AlipayController controller;

    @ApiOperation("统一支付")
    @PostMapping("/test/unified/pay1")
    public Result<UnifiedPayVo> unifiedPa1y(TestUnifiedPayForm form) {
        System.out.println(form);
        return Result.fail();
    }


    @ApiOperation("统一支付")
    @PostMapping("/test/unified/pay")
    public Result<UnifiedPayVo> unifiedPay(@RequestBody TestUnifiedPayForm form) {
        AtomicLong seq = new AtomicLong(0L);
        String goodsOrderId = String.format("%s%s%06d", "G", DateUtil.getSeqString(), (int) seq.getAndIncrement() % 1000000);
        CreatePayOrderForm createPayOrderForm = new CreatePayOrderForm();
        createPayOrderForm.setMchId("10000000");
        createPayOrderForm.setMchOrderNo(goodsOrderId);
        createPayOrderForm.setChannelId("ALIPAY_MOBILE");
        createPayOrderForm.setAmount(Long.valueOf(form.getFee()));
        createPayOrderForm.setCurrency("cny");
        createPayOrderForm.setClientIp("192.168.1.1");
        createPayOrderForm.setDevice("android");
        createPayOrderForm.setSubject("伴置车测试商品");
        createPayOrderForm.setBody("伴置车测试商品的描述信息");
        createPayOrderForm.setExtra("");
        createPayOrderForm.setParam1("");
        createPayOrderForm.setParam2("");
        createPayOrderForm.setNotifyUrl("http://jiutongtang.cn:19060/test");
        String s = orderServiceController.createPayOrder(createPayOrderForm).getData().toString();
        PayForm payForm = new PayForm();
        payForm.setPayOrderId(s);
        payForm.setSign("");
        Result<Map<String, Object>> result = controller.doAliPayMobileReq(payForm);
        UnifiedPayVo vo = new UnifiedPayVo();
        vo.setSign(result.getData().get("payParams").toString());
        Result result1 = Result.success(vo);
        return result1;
    }

    @ApiOperation("统一支付")
    @PostMapping("/unified/pay")
    public Result<Map<String, Object>> unifiedPayQr(UnifiedPayForm form) {
//        AtomicLong seq = new AtomicLong(0L);
//        String goodsOrderId = String.format("%s%s%06d", "G", DateUtil.getSeqString(), (int) seq.getAndIncrement() % 1000000);
        CreatePayOrderForm createPayOrderForm = new CreatePayOrderForm();
        BeanUtils.copyProperties(form, createPayOrderForm);
        String s = orderServiceController.createPayOrder(createPayOrderForm).getData().toString();
        PayForm payForm = new PayForm();
        payForm.setPayOrderId(s);
        payForm.setSign("");
        switch (form.getChannelId()) {
            case PayConstant.WX_APP:
                return controller.doAliPayMobileReq(payForm);
            case PayConstant.WX_JSAPI:
                return controller.doAliPayMobileReq(payForm);
            case PayConstant.WX_NATIVE:
                return controller.doAliPayMobileReq(payForm);
            case PayConstant.WX_MWEB:
                return controller.doAliPayMobileReq(payForm);
            case PayConstant.ALIPAY_MOBILE:
                return controller.doAliPayMobileReq(payForm);
            case PayConstant.ALIPAY_PC:
                return controller.doAliPayMobileReq(payForm);
            case PayConstant.ALIPAY_WAP:
                return controller.doAliPayMobileReq(payForm);
            case PayConstant.ALIPAY_QR:
                return controller.doAliPayQrReq(payForm);
            default:
                return Result.fail();
            //"不支持的支付渠道类型[channelId="+channelId+"]", null, null));
        }
    }
}
