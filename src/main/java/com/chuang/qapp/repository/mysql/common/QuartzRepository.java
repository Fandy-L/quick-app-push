package com.chuang.qapp.repository.mysql.common;

import com.chuang.qapp.entity.mysql.common.QuartzConfig;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author fandy.lin
 */
public interface QuartzRepository extends JpaRepository<QuartzConfig,Integer> {

}
