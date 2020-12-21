package wiki.heh.bald.pay.api.mapper;

import org.apache.ibatis.annotations.Param;
import wiki.heh.bald.pay.api.entity.po.MchInfo;
import wiki.heh.bald.pay.api.entity.po.MchInfoExample;

import java.util.List;

public interface MchInfoMapper {
    int countByExample(MchInfoExample example);

    int deleteByExample(MchInfoExample example);

    int deleteByPrimaryKey(String mchId);

    int insert(MchInfo record);

    int insertSelective(MchInfo record);

    List<MchInfo> selectByExample(MchInfoExample example);

    MchInfo selectByPrimaryKey(@Param("mchId")String mchId);

    int updateByExampleSelective(@Param("record") MchInfo record, @Param("example") MchInfoExample example);

    int updateByExample(@Param("record") MchInfo record, @Param("example") MchInfoExample example);

    int updateByPrimaryKeySelective(MchInfo record);

    int updateByPrimaryKey(MchInfo record);
}