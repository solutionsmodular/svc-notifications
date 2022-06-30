package com.solmod.notification.engine.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class NotificationEngineRepository {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final NamedParameterJdbcTemplate template;

    public NotificationEngineRepository(NamedParameterJdbcTemplate template) {
        this.template = template;
    }


}
