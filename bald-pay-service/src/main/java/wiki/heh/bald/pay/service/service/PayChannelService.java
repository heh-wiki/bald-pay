package wiki.heh.bald.pay.service.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import wiki.heh.bald.pay.service.mapper.PayChannelMapper;
import wiki.heh.bald.pay.service.model.PayChannel;
import wiki.heh.bald.pay.service.model.PayChannelExample;

import java.util.List;

/**
 *
 * @author hehua
 * @date 2017-07-05
 * @version v1.0

 */
@Component
public class PayChannelService {

    @Autowired
    private PayChannelMapper payChannelMapper;

    public PayChannel selectPayChannel(String channelId, String mchId) {
        PayChannelExample example = new PayChannelExample();
        PayChannelExample.Criteria criteria = example.createCriteria();
        criteria.andChannelIdEqualTo(channelId);
        criteria.andMchIdEqualTo(mchId);
        List<PayChannel> payChannelList = payChannelMapper.selectByExample(example);
        if(CollectionUtils.isEmpty(payChannelList)) return null;
        return payChannelList.get(0);
    }

}
