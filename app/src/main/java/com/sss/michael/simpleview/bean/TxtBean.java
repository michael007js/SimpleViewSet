package com.sss.michael.simpleview.bean;

import com.sss.michael.simpleview.view.SimpleCandleView;

public class TxtBean implements SimpleCandleView.OnXyAxisTextRealization<TxtBean> {
    private String txt;

    private int topLevelHigh;
    private int topLevelLow;

    private int bottomLevelHigh;
    private int bottomLevelLow;

    public TxtBean(String txt, int number) {
        this.txt = txt;
        this.topLevelHigh = number;
        this.topLevelLow = number;
        this.bottomLevelHigh = number;
        this.bottomLevelLow = number;
    }

    public TxtBean(String txt, int topLevelHigh, int topLevelLow, int bottomLevelHigh, int bottomLevelLow) {
        this.txt = txt;
        this.topLevelHigh = topLevelHigh;
        this.topLevelLow = topLevelLow;
        this.bottomLevelHigh = bottomLevelHigh;
        this.bottomLevelLow = bottomLevelLow;
    }

    @Override
    public String getText() {
        return txt;
    }

    @Override
    public int getTopLevelHigh() {
        return topLevelHigh;
    }

    @Override
    public int getTopLevelLow() {
        return topLevelLow;
    }

    @Override
    public int getBottomLevelHigh() {
        return bottomLevelHigh;
    }

    @Override
    public int getBottomLevelLow() {
        return bottomLevelLow;
    }


    @Override
    public TxtBean getBean() {
        return this;
    }
}
