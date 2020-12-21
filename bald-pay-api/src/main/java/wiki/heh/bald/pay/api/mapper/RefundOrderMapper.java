package wiki.heh.bald.pay.api.mapper;

import org.apache.ibatis.annotations.Param;
import wiki.heh.bald.pay.api.entity.po.RefundOrder;
import wiki.heh.bald.pay.api.entity.po.RefundOrderExample;

import java.util.List;

public interface RefundOrderMapper {
    int countByExample(RefundOrderExample example);

    int deleteByExample(RefundOrderExample example);

    int deleteByPrimaryKey(String refundOrderId);

    int insert(RefundOrder record);

    int insertSelective(RefundOrder record);

    List<RefundOrder> selectByExample(RefundOrderExample example);

    RefundOrder selectByPrimaryKey(String refundOrderId);

    int updateByExampleSelective(@Param("record") RefundOrder record, @Param("example") RefundOrderExample example);

    int updateByExample(@Param("record") RefundOrder record, @Param("example") RefundOrderExample example);

    int updateByPrimaryKeySelective(RefundOrder record);

    int updateByPrimaryKey(RefundOrder record);
}