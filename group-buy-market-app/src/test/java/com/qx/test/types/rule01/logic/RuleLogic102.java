package com.qx.test.types.rule01.logic;

import cn.bugstack.wrench.design.framework.link.model1.AbstractLogicLink;
import com.qx.test.types.rule01.factory.Rule01TradeRuleFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class RuleLogic102 extends AbstractLogicLink<String, Rule01TradeRuleFactory.DynamicContext, String> {
    @Override
    public String apply(String requestParameter, Rule01TradeRuleFactory.DynamicContext dynamicContext)
            throws Exception {
        log.info("link model02 RuleLogic101");

        return "link model01 单实例链";
    }
}
