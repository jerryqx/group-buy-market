package com.qx.domain.trade.adapter.port;

import com.qx.domain.trade.model.entity.NotifyTaskEntity;

public interface ITradePort {

    String groupBuyNotify(NotifyTaskEntity notifyTask) throws Exception;

}
