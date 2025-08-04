package com.qx.infrastructure.adapter.repository;

import com.qx.infrastructure.dcc.DCCService;
import com.qx.infrastructure.redis.IRedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.function.Supplier;

public abstract class AbstractRepository {
    private final Logger logger = LoggerFactory.getLogger(AbstractRepository.class);

    @Resource
    protected IRedisService redisService;

    @Resource
    protected DCCService dccService;

    protected <T> T getFromCacheOrDb(String cacheKey, Supplier<T> dbFallback) {
        // 判断是否开启缓存
        if (dccService.isCacheOpenSwitch()) {
            // 从缓存中获取
            T cacheResult = redisService.getValue(cacheKey);
            // 缓存存在则直接返回
            if (cacheResult != null) {
                return cacheResult;
            }
            // 缓存不存在直接从数据库获取
            T dbResult = dbFallback.get();
            // 数据库查询接口为空直接返回
            if (null == dbResult) {
                return null;
            }
            redisService.setValue(cacheKey, dbResult);
            return dbResult;

        } else {
            // 缓存未开启，直接从数据库获取
            logger.warn("缓存降级 {}", cacheKey);
            return dbFallback.get();
        }
    }

}
