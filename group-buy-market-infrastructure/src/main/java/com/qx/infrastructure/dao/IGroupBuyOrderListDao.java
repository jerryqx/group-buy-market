package com.qx.infrastructure.dao;

import com.qx.infrastructure.dao.po.GroupBuyOrderList;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IGroupBuyOrderListDao {

    void insert(GroupBuyOrderList groupBuyOrderListReq);

    GroupBuyOrderList queryNoPayMarketPayOrderByOutTradeNo(GroupBuyOrderList groupBuyOrderListReq);

}
