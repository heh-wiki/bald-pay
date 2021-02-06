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
import wiki.heh.bald.pay.mgr.model.RefundOrder;
import wiki.heh.bald.pay.mgr.service.RefundOrderService;
import wiki.heh.bald.pay.mgr.util.PageModel;

import java.util.List;

/**
 * 退款订单
 * @author heh
 */
@Controller
@RequestMapping("/refund_order")
public class RefundOrderController {

    private final static Logger _log = LoggerFactory.getLogger(RefundOrderController.class);

    @Autowired
    private RefundOrderService refundOrderService;

    @RequestMapping("/list.html")
    public String listInput(ModelMap model) {
        return "refund_order/list";
    }

    @RequestMapping("/list")
    @ResponseBody
    public String list(@ModelAttribute RefundOrder refundOrder, Integer pageIndex, Integer pageSize) {
        PageModel pageModel = new PageModel();
        int count = refundOrderService.count(refundOrder);
        if(count <= 0) return JSON.toJSONString(pageModel);
        List<RefundOrder> refundOrderList = refundOrderService.getRefundOrderList((pageIndex-1)*pageSize, pageSize, refundOrder);
        if(!CollectionUtils.isEmpty(refundOrderList)) {
            JSONArray array = new JSONArray();
            for(RefundOrder po : refundOrderList) {
                JSONObject object = (JSONObject) JSONObject.toJSON(po);
                if(po.getCreateTime() != null) object.put("createTime", DateUtil.date2Str(po.getCreateTime()));
                if(po.getRefundAmount() != null) object.put("amount", AmountUtil.convertCent2Dollar(po.getRefundAmount()+""));
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
    public String viewInput(String refundOrderId, ModelMap model) {
        RefundOrder item = null;
        if(StringUtils.isNotBlank(refundOrderId)) {
            item = refundOrderService.selectRefundOrder(refundOrderId);
        }
        if(item == null) {
            item = new RefundOrder();
            model.put("item", item);
            return "refund_order/view";
        }
        JSONObject object = (JSONObject) JSON.toJSON(item);
        if(item.getRefundSuccTime() != null) object.put("refundSuccTime", DateUtil.date2Str(item.getRefundSuccTime()));
        if(item.getExpireTime() != null) object.put("expireTime", DateUtil.date2Str(item.getExpireTime()));
        if(item.getRefundAmount() != null) object.put("amount", AmountUtil.convertCent2Dollar(item.getRefundAmount()+""));
        model.put("item", object);
        return "refund_order/view";
    }

}