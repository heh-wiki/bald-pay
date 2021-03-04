package wiki.heh.bald.pay.mgr.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import wiki.heh.bald.pay.common.constant.PayConstant;
import wiki.heh.bald.pay.common.util.DateUtil;
import wiki.heh.bald.pay.mgr.model.PayChannel;
import wiki.heh.bald.pay.mgr.model.form.GetChannelForm;
import wiki.heh.bald.pay.mgr.model.form.PayChannelForm;
import wiki.heh.bald.pay.mgr.model.form.SearchPayChannel;
import wiki.heh.bald.pay.mgr.model.vo.Result;
import wiki.heh.bald.pay.mgr.service.PayChannelService;
import wiki.heh.bald.pay.mgr.util.PageModel;

import java.util.List;

/**
 * 支付渠道
 *
 * @author heh
 */
@Controller
@RequestMapping("pay_channel")
public class PayChannelController {

    private final static Logger _log = LoggerFactory.getLogger(PayChannelController.class);

    @Autowired
    private PayChannelService payChannelService;

    @GetMapping("list.html")
    public String listInput(ModelMap model) {
        return "pay_channel/list";
    }

    @GetMapping("edit.html")
    public String editInput(String id, ModelMap model) {
        PayChannel item = null;
        if (StringUtils.isNotBlank(id) && NumberUtils.isNumber(id)) {
            item = payChannelService.selectPayChannel(Integer.parseInt(id));
        }
        if (item == null) item = new PayChannel();
        model.put("item", item);
        return "pay_channel/edit";
    }

    @GetMapping("list")
    @ResponseBody
    public String list(@ModelAttribute PayChannel payChannel, Integer pageIndex, Integer pageSize) {
        PageModel pageModel = new PageModel();
        int count = payChannelService.count(payChannel);
        if (count <= 0) return JSON.toJSONString(pageModel);
        List<PayChannel> payChannelList = payChannelService.getPayChannelList((pageIndex - 1) * pageSize, pageSize, payChannel);
        if (!CollectionUtils.isEmpty(payChannelList)) {
            JSONArray array = new JSONArray();
            for (PayChannel pc : payChannelList) {
                JSONObject object = (JSONObject) JSONObject.toJSON(pc);
                object.put("createTime", DateUtil.date2Str(pc.getCreateTime()));
                array.add(object);
            }
            pageModel.setList(array);
        }
        pageModel.setCount(count);
        pageModel.setMsg("ok");
        pageModel.setRel(true);
        return JSON.toJSONString(pageModel);
    }

    @PostMapping("save")
    @ResponseBody
    public String save(@RequestParam String params) {
        JSONObject po = JSONObject.parseObject(params);
        String channelId = po.getString("channelId");
        String param = po.getString("param");
        // 对于配置支付宝参数时,前端将+号转为空格bug处理
        if (PayConstant.PAY_CHANNEL_ALIPAY_MOBILE.equals(channelId) ||
                PayConstant.PAY_CHANNEL_ALIPAY_PC.equals(channelId) ||
                PayConstant.PAY_CHANNEL_ALIPAY_WAP.equals(channelId) ||
                PayConstant.PAY_CHANNEL_ALIPAY_QR.equals(channelId)) {
            JSONObject paramObj = null;
            try {
                paramObj = JSON.parseObject(po.getString("param"));
            } catch (Exception e) {
                _log.info("param is not json");
            }
            if (paramObj != null) {
                paramObj.put("private_key", paramObj.getString("private_key").replaceAll(" ", "+"));
                paramObj.put("alipay_public_key", paramObj.getString("alipay_public_key").replaceAll(" ", "+"));
                param = paramObj.toJSONString();
            }
        }
        PayChannel payChannel = new PayChannel();
        Integer id = po.getInteger("id");
        payChannel.setChannelId(channelId);
        payChannel.setMchId(po.getString("mchId"));
        payChannel.setChannelName(po.getString("channelName"));
        payChannel.setChannelMchId(po.getString("channelMchId"));
        payChannel.setState((byte) ("on".equalsIgnoreCase(po.getString("state")) ? 1 : 0));
        payChannel.setParam(param);
        payChannel.setRemark(po.getString("remark"));
        int result;
        if (id == null) {
            // 添加
            result = payChannelService.addPayChannel(payChannel);
        } else {
            // 修改
            payChannel.setId(id);
            result = payChannelService.updatePayChannel(payChannel);
        }
        _log.info("保存渠道记录,返回:{}", result);
        return result + "";
    }

    @GetMapping("view.html")
    public String viewInput(String id, ModelMap model) {
        PayChannel item = null;
        if (StringUtils.isNotBlank(id) && NumberUtils.isNumber(id)) {
            item = payChannelService.selectPayChannel(Integer.parseInt(id));
        }
        if (item == null) item = new PayChannel();
        model.put("item", item);
        return "pay_channel/view";
    }

    @PostMapping("save/v1")
    @ResponseBody
    public String save(@RequestBody PayChannelForm form) {
        JSONObject po = JSONObject.parseObject(JSON.toJSONString(form));
        String channelId = po.getString("channelId");
        String param = po.getString("param");
        // 对于配置支付宝参数时,前端将+号转为空格bug处理
        if (PayConstant.PAY_CHANNEL_ALIPAY_MOBILE.equals(channelId) ||
                PayConstant.PAY_CHANNEL_ALIPAY_PC.equals(channelId) ||
                PayConstant.PAY_CHANNEL_ALIPAY_WAP.equals(channelId) ||
                PayConstant.PAY_CHANNEL_ALIPAY_QR.equals(channelId)) {
            JSONObject paramObj = null;
            try {
                paramObj = JSON.parseObject(po.getString("param"));
            } catch (Exception e) {
                _log.info("param is not json");
            }
            if (paramObj != null) {
                paramObj.put("private_key", paramObj.getString("private_key").replaceAll(" ", "+"));
                paramObj.put("alipay_public_key", paramObj.getString("public_key").replaceAll(" ", "+"));
                param = paramObj.toJSONString();
            }
        }
        PayChannel payChannel = new PayChannel();
        Integer id = po.getInteger("id");
        payChannel.setChannelId(channelId);
        payChannel.setMchId(po.getString("mchId"));
        payChannel.setChannelName(po.getString("channelName"));
        payChannel.setChannelMchId(po.getString("channelMchId"));
        payChannel.setState((byte) ("on".equalsIgnoreCase(po.getString("state")) ? 1 : 0));
        payChannel.setParam(param);
        payChannel.setRemark(po.getString("remark"));
        int result;
        if (id == null) {
            // 添加
            result = payChannelService.addPayChannel(payChannel);
        } else {
            // 修改
            payChannel.setId(id);
            result = payChannelService.updatePayChannel(payChannel);
        }
        _log.info("保存渠道记录,返回:{}", result);
        return result + "";
    }

    @PostMapping("get/v1")
    @ResponseBody
    public String get(@RequestBody GetChannelForm form) {
        PayChannel payChannel = payChannelService.selectPayChannel(form.getChannelId(), form.getMchId());
        return JSON.toJSONString(Result.success(payChannel));
    }

    @PostMapping("list/v1")
    @ResponseBody
    public String channelList(@RequestBody SearchPayChannel payChannel) {
        PageModel pageModel = new PageModel();
        PayChannel payChannel1 = new PayChannel();
        BeanUtils.copyProperties(payChannel, payChannel1);
        int count = payChannelService.count(payChannel1);
        if (count <= 0) return JSON.toJSONString(pageModel);
        List<PayChannel> payChannelList = payChannelService.getPayChannelList((payChannel.getPageIndex() - 1) * payChannel.getPageSize(), payChannel.getPageSize(), payChannel1);
        if (!CollectionUtils.isEmpty(payChannelList)) {
            JSONArray array = new JSONArray();
            for (PayChannel pc : payChannelList) {
                JSONObject object = (JSONObject) JSONObject.toJSON(pc);
                object.put("createTime", DateUtil.date2Str(pc.getCreateTime()));
                array.add(object);
            }
            pageModel.setList(array);
        }

        pageModel.setCount(count);
        pageModel.setMsg("ok");
        pageModel.setRel(true);
        return JSON.toJSONString(pageModel);
    }
}