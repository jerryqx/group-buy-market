package com.qx.test.infrastructure.dao;

import com.qx.infrastructure.dao.IGroupBuyActivityDao;
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
public class GroupBuyActivityDaoTest {

    @Resource
    private IGroupBuyActivityDao groupBuyActivityDao;

    @Test
    public void test_queryGroupBuyActivityList() {
        log.info("{}", groupBuyActivityDao.queryGroupBuyActivityList());
    }
}
