package com.qx.api;

import com.qx.api.dto.GoodsMarketRequestDTO;
import com.qx.api.dto.GoodsMarketResponseDTO;
import com.qx.api.response.Response;

public interface IMarketIndexService {

    Response<GoodsMarketResponseDTO> queryGroupBuyMarketConfig(GoodsMarketRequestDTO requestDTO);
}
