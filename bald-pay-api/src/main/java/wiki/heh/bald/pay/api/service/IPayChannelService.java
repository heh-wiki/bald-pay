package wiki.heh.bald.pay.api.service;

import com.alibaba.fastjson.JSONObject;

import java.util.Map;

/**
 *
 * @author heh
 * @version v1.0
 * @date 2020-12-18
 */
public interface IPayChannelService {

    Map selectPayChannel(String jsonParam);

    JSONObject getByMchIdAndChannelId(String mchId, String channelId);
}
