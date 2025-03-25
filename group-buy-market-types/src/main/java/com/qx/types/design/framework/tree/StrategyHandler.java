package com.qx.types.design.framework.tree;

/**
 * Function: 策略处理器
 *
 * @author 秦啸
 */
public interface StrategyHandler<T, D, R> {

    StrategyHandler DEFAULT = (T, D) -> null;

    R apply(T requestParameter, D dynamicContext) throws Exception;
}
