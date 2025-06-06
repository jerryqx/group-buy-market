package com.qx.domain.activity.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Function:
 *
 * @author 秦啸
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SkuVO {


    /**
     * 商品ID
     */
    private String goodsId;

    /**
     * 商品名称
     */
    private String goodsName;

    /**
     * 商品价格
     */
    private BigDecimal originalPrice;

}
