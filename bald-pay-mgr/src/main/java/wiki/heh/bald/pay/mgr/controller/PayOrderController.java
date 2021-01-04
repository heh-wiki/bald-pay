package wiki.heh.bald.pay.mgr.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import wiki.heh.bald.pay.common.util.AmountUtil;
import wiki.heh.bald.pay.common.util.DateUtil;
import wiki.heh.bald.pay.mgr.model.PayOrder;
import wiki.heh.bald.pay.mgr.service.PayOrderService;
import wiki.heh.bald.pay.mgr.util.PageModel;

import java.util.Date;
import java.util.List;

/**
 * 支付订单
 */
@Controller
@RequestMapping("/pay_order")
public class PayOrderController {

    private final static Logger _log = LoggerFactory.getLogger(PayOrderController.class);

    @Autowired
    private PayOrderService payOrderService;

    @RequestMapping("/list.html")
    public String listInput(ModelMap model) {
        return "pay_order/list";
    }

    @RequestMapping("/list")
    @ResponseBody
    public String list(@ModelAttribute PayOrder payOrder, Integer pageIndex, Integer pageSize) {
        PageModel pageModel = new PageModel();
        int count = payOrderService.count(payOrder);
        if(count <= 0) return JSON.toJSONString(pageModel);
        List<PayOrder> payOrderList = payOrderService.getPayOrderList((pageIndex-1)*pageSize, pageSize, payOrder);
        if(!CollectionUtils.isEmpty(payOrderList)) {
            JSONArray array = new JSONArray();
            for(PayOrder po : payOrderList) {
                JSONObject object = (JSONObject) JSONObject.toJSON(po);
                if(po.getCreateTime() != null) object.put("createTime", DateUtil.date2Str(po.getCreateTime()));
                if(po.getAmount() != null) object.put("amount", AmountUtil.convertCent2Dollar(po.getAmount()+""));
                array.add(object);
            }
            pageModel.setList(array);
        }
        pageModel.setCount(count);
        pageModel.setMsg("ok");
        pageModel.setRel(true);
        return JSON.toJSONString(pageModel);
    }

    @RequestMapping("/view.html")
    public String viewInput(String payOrderId, ModelMap model) {
        PayOrder item = null;
        if(StringUtils.isNotBlank(payOrderId)) {
            item = payOrderService.selectPayOrder(payOrderId);
        }
        if(item == null) {
            item = new PayOrder();
            model.put("item", item);
            return "pay_order/view";
        }
        JSONObject object = (JSONObject) JSON.toJSON(item);
        if(item.getPaySuccTime() != null) object.put("paySuccTime", DateUtil.date2Str(new Date(item.getPaySuccTime())));
        if(item.getLastNotifyTime() != null) object.put("lastNotifyTime", DateUtil.date2Str(new Date(item.getLastNotifyTime())));
        if(item.getExpireTime() != null) object.put("expireTime", DateUtil.date2Str(new Date(item.getExpireTime())));
        if(item.getAmount() != null) object.put("amount", AmountUtil.convertCent2Dollar(item.getAmount()+""));
        model.put("item", object);
        return "pay_order/view";
    }

}