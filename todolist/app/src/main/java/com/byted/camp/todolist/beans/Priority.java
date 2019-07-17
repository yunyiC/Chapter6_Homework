package com.byted.camp.todolist.beans;

import android.graphics.Color;

/**
 * @author Administrator
 * @date 2019/7/17
 */
public enum Priority {

    /**
     * 非常重要
     */
    VERY_IMPORTANT(2, Color.RED),

    /**
     * 重要
     */
    IMPORTANT(1, Color.YELLOW),

    /**
     * 一般
     */
    NORMAL(0, Color.WHITE);

    public final int intValue;
    public final int color;

    Priority(int intValue, int color) {
        this.intValue = intValue;
        this.color = color;
    }

    public static Priority from(int intValue) {
        for (Priority priority : Priority.values()) {
            if (priority.intValue == intValue) {
                return priority;
            }
        }

        // default
        return NORMAL;
    }
}
