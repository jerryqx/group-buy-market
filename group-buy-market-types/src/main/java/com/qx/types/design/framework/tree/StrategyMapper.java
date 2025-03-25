package com.qx.types.design.framework.tree;

/**
 * Function:
 * 策略映射器
 * @author 秦啸
 */
public interface StrategyMapper<T, D, R> {

    /**
     * 获取待执行策略
     * @param requestParameter 入参
     * @param dynamicContext 上下文
     * @return 返参
     */
    StrategyHandler<T, D, R> get(T requestParameter, D dynamicContext) throws Exception;
}
