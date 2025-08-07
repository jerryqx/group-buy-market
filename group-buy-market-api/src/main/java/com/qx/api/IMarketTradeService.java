package com.qx.api;

import com.qx.api.dto.LockMarketPayOrderRequestDTO;
import com.qx.api.dto.LockMarketPayOrderResponseDTO;
import com.qx.api.dto.RefundMarketPayOrderRequestDTO;
import com.qx.api.dto.RefundMarketPayOrderResponseDTO;
import com.qx.api.dto.SettlementMarketPayOrderRequestDTO;
import com.qx.api.dto.SettlementMarketPayOrderResponseDTO;
import com.qx.api.response.Response;

/**
 * 营销交易服务接口
 */
public interface IMarketTradeService {

    Response<LockMarketPayOrderResponseDTO> lockMarketPayOrder(
            LockMarketPayOrderRequestDTO lockMarketPayOrderRequestDTO);

    Response<SettlementMarketPayOrderResponseDTO> settlementMarketPayOrder(
            SettlementMarketPayOrderRequestDTO requestDTO);

    /**
     * 营销拼团退单
     *
     * @param requestDTO 退单请求信息
     * @return 退单结果信息
     */
    Response<RefundMarketPayOrderResponseDTO> refundMarketPayOrder(RefundMarketPayOrderRequestDTO requestDTO);


}
