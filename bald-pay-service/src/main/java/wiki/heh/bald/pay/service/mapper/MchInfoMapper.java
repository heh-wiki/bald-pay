package wiki.heh.bald.pay.service.mapper;

import org.apache.ibatis.annotations.Param;
import wiki.heh.bald.pay.service.model.MchInfo;
import wiki.heh.bald.pay.service.model.MchInfoExample;

import java.util.List;

public interface MchInfoMapper {
    int countByExample(MchInfoExample example);

    int deleteByExample(MchInfoExample example);

    int deleteByPrimaryKey(String mchId);

    int insert(MchInfo record);

    int insertSelective(MchInfo record);

    List<MchInfo> selectByExample(MchInfoExample example);

    MchInfo selectByPrimaryKey(String mchId);

    int updateByExampleSelective(@Param("record") MchInfo record, @Param("example") MchInfoExample example);

    int updateByExample(@Param("record") MchInfo record, @Param("example") MchInfoExample example);

    int updateByPrimaryKeySelective(MchInfo record);

    int updateByPrimaryKey(MchInfo record);
}