package wiki.heh.bald.pay.service.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import wiki.heh.bald.pay.service.mapper.MchInfoMapper;
import wiki.heh.bald.pay.service.model.MchInfo;

import javax.annotation.Resource;

/**
 *
 * @author hehua
 * @date 2017-07-05
 * @version v1.0

 */
@Component
public class MchInfoService {

    @Resource
    private MchInfoMapper mchInfoMapper;

    public MchInfo selectMchInfo(String mchId) {
        return mchInfoMapper.selectByPrimaryKey(mchId);
    }

}
