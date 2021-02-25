package wiki.heh.bald.pay.mgr.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import wiki.heh.bald.pay.common.util.DateUtil;
import wiki.heh.bald.pay.mgr.model.MchInfo;
import wiki.heh.bald.pay.mgr.model.form.MchInfoParam;
import wiki.heh.bald.pay.mgr.model.vo.Result;
import wiki.heh.bald.pay.mgr.service.MchInfoService;
import wiki.heh.bald.pay.mgr.util.PageModel;

import java.util.List;

@Controller
@RequestMapping("mch_info")
public class MchInfoController {

    private final static Logger _log = LoggerFactory.getLogger(MchInfoController.class);
    private final MchInfoService mchInfoService;

    public MchInfoController(MchInfoService mchInfoService) {
        this.mchInfoService = mchInfoService;
    }

    @GetMapping("list.html")
    public String listInput(ModelMap model) {
        return "mch_info/list";
    }

    @GetMapping("edit.html")
    public String editInput(String mchId, ModelMap model) {
        MchInfo item = null;
        if (StringUtils.isNotBlank(mchId)) {
            item = mchInfoService.selectMchInfo(mchId);
        }
        if (item == null) item = new MchInfo();
        model.put("item", item);
        return "mch_info/edit";
    }

    @GetMapping("list")
    @ResponseBody
    public String list(@ModelAttribute MchInfo mchInfo, Integer pageIndex, Integer pageSize) {
        PageModel pageModel = new PageModel();
        int count = mchInfoService.count(mchInfo);
        if (count <= 0) return JSON.toJSONString(pageModel);
        List<MchInfo> mchInfoList = mchInfoService.getMchInfoList((pageIndex - 1) * pageSize, pageSize, mchInfo);
        if (!CollectionUtils.isEmpty(mchInfoList)) {
            JSONArray array = new JSONArray();
            for (MchInfo mi : mchInfoList) {
                JSONObject object = (JSONObject) JSONObject.toJSON(mi);
                object.put("createTime", DateUtil.date2Str(mi.getCreateTime()));
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
        MchInfo mchInfo = new MchInfo();
        String mchId = po.getString("mchId");
        mchInfo.setName(po.getString("name"));
        mchInfo.setType(po.getString("type"));
        mchInfo.setState((byte) ("on".equalsIgnoreCase(po.getString("state")) ? 1 : 0));
        mchInfo.setReqKey(po.getString("reqKey"));
        mchInfo.setResKey(po.getString("resKey"));
        int result;
        if (StringUtils.isBlank(mchId)) {
            // 添加
            result = mchInfoService.addMchInfo(mchInfo);
        } else {
            // 修改
            mchInfo.setMchId(mchId);
            result = mchInfoService.updateMchInfo(mchInfo);
        }
        _log.info("保存商户记录,返回:{}", result);
        return result + "";
    }

    @GetMapping("view.html")
    public String viewInput(String mchId, ModelMap model) {
        MchInfo item = null;
        if (StringUtils.isNotBlank(mchId)) {
            item = mchInfoService.selectMchInfo(mchId);
        }
        if (item == null) item = new MchInfo();
        model.put("item", item);
        return "mch_info/view";
    }

    @GetMapping("{id}/v1")
    @ResponseBody
    public String get(@PathVariable String id) {
        MchInfo mchInfo = mchInfoService.selectMchInfo(id);
        return JSON.toJSONString(Result.success(mchInfo));
    }

    @PostMapping("save/v1")
    @ResponseBody
    public String save(@RequestBody MchInfoParam params) {
        _log.info("请求保存商户记录");
        MchInfo mchInfo = new MchInfo();
        BeanUtils.copyProperties(params, mchInfo);
        int result;
        if (StringUtils.isBlank(params.getMchId())) {
            // 添加
            result = mchInfoService.addMchInfo(mchInfo);
        } else {
            // 修改
            result = mchInfoService.updateMchInfo(mchInfo);
        }
        _log.info("保存商户记录,返回:{}", result);
        return result + "";
    }

}