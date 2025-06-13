package com.qx.types.design.framework.link.model2.handler;

public interface ILogicHandler<T, D, R> {
    R apply(T requestParameter, D dynamicContext) throws Exception;
}
