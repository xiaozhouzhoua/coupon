package com.core.schedule;

import com.core.dao.CouponTemplateDao;
import com.core.entity.CouponTemplate;
import com.core.vo.TemplateRule;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 定时清理已过期的优惠劵模板
 */
@Slf4j
@Component
public class ScheduledTask {
    @Autowired
    private CouponTemplateDao templateDao;

    /**
     * 下线已过期的优惠劵模板<每1小时清理一次>
     */
    @Scheduled(fixedRate = 60 * 60 * 1000)
    public void offlineCouponTemplate() {
        log.info("开始清理已过期优惠劵模板");
        List<CouponTemplate> templates = templateDao.findAllByExpired(false);
        if (CollectionUtils.isEmpty(templates)) {
            log.info("没有可用优惠劵数据！");
            return;
        }
        Date cur = new Date();
        List<CouponTemplate> expiredTemplates = new ArrayList<>(templates.size());
        templates.forEach(t -> {
            // 根据优惠劵模板规则中的"过期规则"校验模板是否过期
            TemplateRule rule = t.getRule();
            if (rule.getExpiration().getDeadline() < cur.getTime()) {
                t.setExpired(true);
                expiredTemplates.add(t);
            }
        });
        if (CollectionUtils.isNotEmpty(expiredTemplates)) {
            log.info("过期的优惠劵模板数: {}",
                    templateDao.saveAll(expiredTemplates));
        }
        log.info("结束清理已过期优惠劵模板");
    }
}
