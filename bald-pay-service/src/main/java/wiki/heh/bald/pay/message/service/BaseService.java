//package wiki.heh.bald.pay.message.service;
//
//import org.springframework.stereotype.Service;
//import org.springframework.util.CollectionUtils;
//import wiki.heh.bald.pay.api.mapper.MchInfoMapper;
//import wiki.heh.bald.pay.api.mapper.MchNotifyMapper;
//import wiki.heh.bald.pay.api.mapper.PayChannelMapper;
//import wiki.heh.bald.pay.api.mapper.PayOrderMapper;
//import wiki.heh.bald.pay.common.constant.PayConstant;
//
//import javax.annotation.Resource;
//import java.util.Date;
//import java.util.LinkedList;
//import java.util.List;
//
///**
// * @author heh
// * @version v1.0
// * @date 2020-12-18
// */
//@Service
//public class BaseService {
//
//    @Resource
//    private PayOrderMapper payOrderMapper;
//
//    @Resource
//    private MchInfoMapper mchInfoMapper;
//
//    @Resource
//    private MchNotifyMapper mchNotifyMapper;
//
//    @Resource
//    private PayChannelMapper payChannelMapper;
//
//    public MchNotify baseSelectMchNotify(String orderId) {
//        return mchNotifyMapper.selectByPrimaryKey(orderId);
//    }
//
//    public int baseInsertMchNotify(String orderId, String mchId, String mchOrderNo, String orderType, String notifyUrl) {
//        MchNotify mchNotify = new MchNotify();
//        mchNotify.setOrderId(orderId);
//        mchNotify.setMchId(mchId);
//        mchNotify.setMchOrderNo(mchOrderNo);
//        mchNotify.setOrderType(orderType);
//        mchNotify.setNotifyUrl(notifyUrl);
//        return mchNotifyMapper.insertSelectiveOnDuplicateKeyUpdate(mchNotify);
//    }
//
//    public int baseUpdateMchNotifySuccess(String orderId, String result, byte notifyCount) {
//        MchNotify mchNotify = new MchNotify();
//        mchNotify.setStatus(PayConstant.MCH_NOTIFY_STATUS_SUCCESS);
//        mchNotify.setResult(result);
//        mchNotify.setNotifyCount(notifyCount);
//        mchNotify.setLastNotifyTime(new Date());
//        MchNotifyExample example = new MchNotifyExample();
//        MchNotifyExample.Criteria criteria = example.createCriteria();
//        criteria.andOrderIdEqualTo(orderId);
//        List values = new LinkedList<>();
//        values.add(PayConstant.MCH_NOTIFY_STATUS_NOTIFYING);
//        values.add(PayConstant.MCH_NOTIFY_STATUS_FAIL);
//        criteria.andStatusIn(values);
//        return mchNotifyMapper.updateByExampleSelective(mchNotify, example);
//    }
//
//    public int baseUpdateMchNotifyFail(String orderId, String result, byte notifyCount) {
//        MchNotify mchNotify = new MchNotify();
//        mchNotify.setStatus(PayConstant.MCH_NOTIFY_STATUS_FAIL);
//        mchNotify.setResult(result);
//        mchNotify.setNotifyCount(notifyCount);
//        mchNotify.setLastNotifyTime(new Date());
//        MchNotifyExample example = new MchNotifyExample();
//        MchNotifyExample.Criteria criteria = example.createCriteria();
//        criteria.andOrderIdEqualTo(orderId);
//        List values = new LinkedList<>();
//        values.add(PayConstant.MCH_NOTIFY_STATUS_NOTIFYING);
//        values.add(PayConstant.MCH_NOTIFY_STATUS_FAIL);
//        return mchNotifyMapper.updateByExampleSelective(mchNotify, example);
//    }
//
//
//    public MchInfo baseSelectMchInfo(String mchId) {
//        return mchInfoMapper.selectByPrimaryKey(mchId);
//    }
//
//    public PayChannel baseSelectPayChannel(String mchId, String channelId) {
//        PayChannelExample example = new PayChannelExample();
//        PayChannelExample.Criteria criteria = example.createCriteria();
//        criteria.andChannelIdEqualTo(channelId);
//        criteria.andMchIdEqualTo(mchId);
//        List<PayChannel> payChannelList = payChannelMapper.selectByExample(example);
//        if (CollectionUtils.isEmpty(payChannelList)) return null;
//        return payChannelList.get(0);
//    }
//
//    public int baseCreatePayOrder(PayOrder payOrder) {
//        System.out.println("****************************\n" + payOrder.toString());
//        return payOrderMapper.insertSelective(payOrder);
//    }
//
//    public PayOrder baseSelectPayOrder(String payOrderId) {
//        return payOrderMapper.selectByPrimaryKey(payOrderId);
//    }
//
//    public PayOrder baseSelectPayOrderByMchIdAndPayOrderId(String mchId, String payOrderId) {
//        PayOrderExample example = new PayOrderExample();
//        PayOrderExample.Criteria criteria = example.createCriteria();
//        criteria.andMchIdEqualTo(mchId);
//        criteria.andPayOrderIdEqualTo(payOrderId);
//        List<PayOrder> payOrderList = payOrderMapper.selectByExample(example);
//        return CollectionUtils.isEmpty(payOrderList) ? null : payOrderList.get(0);
//    }
//
//    public PayOrder baseSelectPayOrderByMchIdAndMchOrderNo(String mchId, String mchOrderNo) {
//        PayOrderExample example = new PayOrderExample();
//        PayOrderExample.Criteria criteria = example.createCriteria();
//        criteria.andMchIdEqualTo(mchId);
//        criteria.andMchOrderNoEqualTo(mchOrderNo);
//        List<PayOrder> payOrderList = payOrderMapper.selectByExample(example);
//        return CollectionUtils.isEmpty(payOrderList) ? null : payOrderList.get(0);
//    }
//
//    public int baseUpdateStatus4Ing(String payOrderId, String channelOrderNo) {
//        PayOrder payOrder = new PayOrder();
//        payOrder.setStatus(PayConstant.PAY_STATUS_PAYING);
//        if (channelOrderNo != null) payOrder.setChannelOrderNo(channelOrderNo);
//        payOrder.setPaySuccTime(System.currentTimeMillis());
//        PayOrderExample example = new PayOrderExample();
//        PayOrderExample.Criteria criteria = example.createCriteria();
//        criteria.andPayOrderIdEqualTo(payOrderId);
//        criteria.andStatusEqualTo(PayConstant.PAY_STATUS_INIT);
//        return payOrderMapper.updateByExampleSelective(payOrder, example);
//    }
//
//    public int baseUpdateStatus4Success(String payOrderId, String channelOrderNo) {
//        PayOrder payOrder = new PayOrder();
//        payOrder.setPayOrderId(payOrderId);
//        payOrder.setStatus(PayConstant.PAY_STATUS_SUCCESS);
//        if (channelOrderNo != null) payOrder.setChannelOrderNo(channelOrderNo);
//        payOrder.setPaySuccTime(System.currentTimeMillis());
//        PayOrderExample example = new PayOrderExample();
//        PayOrderExample.Criteria criteria = example.createCriteria();
//        criteria.andPayOrderIdEqualTo(payOrderId);
//        criteria.andStatusEqualTo(PayConstant.PAY_STATUS_PAYING);
//        return payOrderMapper.updateByExampleSelective(payOrder, example);
//    }
//
//    public int baseUpdateStatus4Complete(String payOrderId) {
//        PayOrder payOrder = new PayOrder();
//        payOrder.setPayOrderId(payOrderId);
//        payOrder.setStatus(PayConstant.PAY_STATUS_COMPLETE);
//        PayOrderExample example = new PayOrderExample();
//        PayOrderExample.Criteria criteria = example.createCriteria();
//        criteria.andPayOrderIdEqualTo(payOrderId);
//        criteria.andStatusEqualTo(PayConstant.PAY_STATUS_SUCCESS);
//        return payOrderMapper.updateByExampleSelective(payOrder, example);
//    }
//
//    public int baseUpdateNotify(String payOrderId, byte count) {
//        PayOrder newPayOrder = new PayOrder();
//        newPayOrder.setNotifyCount(count);
//        newPayOrder.setLastNotifyTime(System.currentTimeMillis());
//        newPayOrder.setPayOrderId(payOrderId);
//        return payOrderMapper.updateByPrimaryKeySelective(newPayOrder);
//    }
//
//    public int baseUpdateNotify(PayOrder payOrder) {
//        return payOrderMapper.updateByPrimaryKeySelective(payOrder);
//    }
//
//}
