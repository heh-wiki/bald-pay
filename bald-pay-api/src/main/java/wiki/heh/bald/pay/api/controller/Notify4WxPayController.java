package wiki.heh.bald.pay.api.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wiki.heh.bald.pay.api.service.INotifyPayService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * 接收处理微信通知
 *
 * @author heh
 * @version v1.0
 * @date 2020-12-18
 */
@Api(tags = "接收处理微信通知")
@RestController
public class Notify4WxPayController {

    private static final Logger _log = LoggerFactory.getLogger(Notify4WxPayController.class);

    @Autowired
    private INotifyPayService notifyPayService;


    @ApiOperation(("微信支付(统一下单接口)后台通知响应"))
    @RequestMapping("/notify/pay/wxPayNotifyRes.htm")
    public String wxPayNotifyRes(HttpServletRequest request) throws ServletException, IOException {
        _log.info("====== 开始接收微信支付回调通知 ======");
        String notifyRes = doWxPayRes(request);
        _log.info("响应给微信:{}", notifyRes);
        _log.info("====== 完成接收微信支付回调通知 ======");
        return notifyRes;
    }

    public String doWxPayRes(HttpServletRequest request) throws ServletException, IOException {
        String logPrefix = "【微信支付回调通知】";
        String xmlResult = IOUtils.toString(request.getInputStream(), request.getCharacterEncoding());
        _log.info("{}通知请求数据:reqStr={}", logPrefix, xmlResult);
        return notifyPayService.handleWxPayNotify(xmlResult);
    }

}
