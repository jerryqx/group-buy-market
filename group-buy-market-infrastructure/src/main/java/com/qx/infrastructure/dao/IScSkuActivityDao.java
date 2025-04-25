package com.qx.infrastructure.dao;

import com.qx.infrastructure.dao.po.SCSkuActivity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IScSkuActivityDao {
    SCSkuActivity querySCSkuActivityBySCGoodsId(SCSkuActivity scSkuActivity);

}