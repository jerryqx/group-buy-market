package com.qx.api;

import com.qx.api.dto.LockMarketPayOrderRequestDTO;
import com.qx.api.dto.LockMarketPayOrderResponseDTO;
import com.qx.api.response.Response;

/**
 * 营销交易服务接口
 */
public interface IMarketTradeService {

    Response<LockMarketPayOrderResponseDTO> lockMarketPayOrder(LockMarketPayOrderRequestDTO lockMarketPayOrderRequestDTO);

}
