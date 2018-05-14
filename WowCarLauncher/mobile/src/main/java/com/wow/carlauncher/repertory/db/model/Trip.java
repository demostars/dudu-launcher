package com.wow.carlauncher.repertory.db.model;

import com.wow.frame.repertory.dbTool.BaseEntity;

/**
 * Created by 10124 on 2018/5/12.
 */

public class Trip extends BaseEntity {
    private Long startTime;
    private Integer mileage;//里程 米

    public Integer getMileage() {
        return mileage;
    }

    public Trip setMileage(Integer mileage) {
        this.mileage = mileage;
        return this;
    }

    public Long getStartTime() {
        return startTime;
    }

    public Trip setStartTime(Long startTime) {
        this.startTime = startTime;
        return this;
    }
}
