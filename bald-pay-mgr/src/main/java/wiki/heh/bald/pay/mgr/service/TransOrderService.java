package wiki.heh.bald.pay.mgr.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import wiki.heh.bald.pay.mgr.mapper.TransOrderMapper;
import wiki.heh.bald.pay.mgr.model.TransOrder;
import wiki.heh.bald.pay.mgr.model.TransOrderExample;

import java.util.List;

/**
 *
 * @author heh
 * @version v1.0
 * @date 2020-12-18
 */
@Component
public class TransOrderService {

    @Autowired
    private TransOrderMapper transOrderMapper;

    public TransOrder selectTransOrder(String transOrderId) {
        return transOrderMapper.selectByPrimaryKey(transOrderId);
    }

    public List<TransOrder> getTransOrderList(int offset, int limit, TransOrder transOrder) {
        TransOrderExample example = new TransOrderExample();
        example.setOrderByClause("createTime DESC");
        example.setOffset(offset);
        example.setLimit(limit);
        TransOrderExample.Criteria criteria = example.createCriteria();
        setCriteria(criteria, transOrder);
        return transOrderMapper.selectByExample(example);
    }

    public Integer count(TransOrder transOrder) {
        TransOrderExample example = new TransOrderExample();
        TransOrderExample.Criteria criteria = example.createCriteria();
        setCriteria(criteria, transOrder);
        return transOrderMapper.countByExample(example);
    }

    void setCriteria(TransOrderExample.Criteria criteria, TransOrder transOrder) {
        if(transOrder != null) {
            if(StringUtils.isNotBlank(transOrder.getMchId())) criteria.andMchIdEqualTo(transOrder.getMchId());
            if(StringUtils.isNotBlank(transOrder.getTransOrderId())) criteria.andTransOrderIdEqualTo(transOrder.getTransOrderId());
            if(StringUtils.isNotBlank(transOrder.getMchTransNo())) criteria.andMchTransNoEqualTo(transOrder.getMchTransNo());
            if(StringUtils.isNotBlank(transOrder.getChannelOrderNo())) criteria.andChannelOrderNoEqualTo(transOrder.getChannelOrderNo());
            if(transOrder.getStatus() != null && transOrder.getStatus() != -99) criteria.andStatusEqualTo(transOrder.getStatus());
        }
    }

}
