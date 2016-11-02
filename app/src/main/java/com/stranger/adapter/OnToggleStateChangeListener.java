package com.stranger.adapter;

public interface OnToggleStateChangeListener {
    /**
     * 当开关状态改变回调此方法
     * 
     * @param b
     *            当前开关的最新状态
     */
    void onToggleStateChange(boolean b);
}
