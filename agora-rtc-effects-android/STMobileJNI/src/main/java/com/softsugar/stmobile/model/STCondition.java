package com.softsugar.stmobile.model;

/**
 * Created by softsugar on 18-5-8.
 */

public class STCondition {
    private int preStateModuleId;
    private int preState;
    private STTriggerEvent[] triggers;         ///< \~Chinese 触发事件数组
    private int triggerCount;                  ///< \~Chinese 触发事件数组的长度

    public int getPreStateModuleId() {
        return preStateModuleId;
    }

    public void setPreStateModuleId(int preStateModuleId) {
        this.preStateModuleId = preStateModuleId;
    }

    public int getPreState() {
        return preState;
    }

    public void setPreState(int preState) {
        this.preState = preState;
    }

    public STTriggerEvent[] getTriggers() {
        return triggers;
    }

    public void setTriggers(STTriggerEvent[] triggers) {
        this.triggers = triggers;
    }

    public int getTriggerCount() {
        return triggerCount;
    }

    public void setTriggerCount(int triggerCount) {
        this.triggerCount = triggerCount;
    }
}
