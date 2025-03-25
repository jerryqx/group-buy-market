package com.qx.test.infrastructure.dao;

import com.qx.infrastructure.dao.IGroupBuyDiscountDao;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

/**
 * Function:
 *
 * @author 秦啸
 */
@SpringBootTest
@RunWith(SpringRunner.class)
@Slf4j
public class GroupBuyDiscountDaoTest {

    @Resource
    private IGroupBuyDiscountDao groupBuyDiscountDao;

    @Test
    public void test_queryGroupBuyDiscountList() {
        log.info("{}", groupBuyDiscountDao.queryGroupBuyDiscountList());
    }
}
