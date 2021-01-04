package wiki.heh.bald.pay.mgr.mapper;

import org.apache.ibatis.annotations.Param;
import wiki.heh.bald.pay.mgr.model.PayOrder;
import wiki.heh.bald.pay.mgr.model.PayOrderExample;

import java.util.List;

public interface PayOrderMapper {
    int countByExample(PayOrderExample example);

    int deleteByExample(PayOrderExample example);

    int deleteByPrimaryKey(String payOrderId);

    int insert(PayOrder record);

    int insertSelective(PayOrder record);

    List<PayOrder> selectByExample(PayOrderExample example);

    PayOrder selectByPrimaryKey(String payOrderId);

    int updateByExampleSelective(@Param("record") PayOrder record, @Param("example") PayOrderExample example);

    int updateByExample(@Param("record") PayOrder record, @Param("example") PayOrderExample example);

    int updateByPrimaryKeySelective(PayOrder record);

    int updateByPrimaryKey(PayOrder record);
}