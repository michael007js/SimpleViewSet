package com.sss.michael.simpleview.bean;

import java.util.List;

public class BottomBarModel {
    private int height;
    private int reserveAreaHeight;
    private int pattingTop;
    private int pattingBottom;
    private int selectTab;
    private List<Tab> tabs;

    public int getReserveAreaHeight() {
        return reserveAreaHeight;
    }

    public void setReserveAreaHeight(int reserveAreaHeight) {
        this.reserveAreaHeight = reserveAreaHeight;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getPattingTop() {
        return pattingTop;
    }

    public void setPattingTop(int pattingTop) {
        this.pattingTop = pattingTop;
    }

    public int getPattingBottom() {
        return pattingBottom;
    }

    public void setPattingBottom(int pattingBottom) {
        this.pattingBottom = pattingBottom;
    }

    public int getSelectTab() {
        return selectTab;
    }

    public void setSelectTab(int selectTab) {
        this.selectTab = selectTab;
    }

    public List<Tab> getTabs() {
        return tabs;
    }

    public void setTabs(List<Tab> tabs) {
        this.tabs = tabs;
    }

    public static class Tab {
        private int fragmentIndex;
        private boolean bigImage;
        private int imageWidth;
        private int imageHeight;
        private String pageUrl;
        private String checkedUrl;
        private String unCheckedUrl;
        private float weight;
        private String label;
        private String moduleName;
        private int betweenImageAndText;
        private int textOffsetY;
        private int textSize;
        private String checkTextColor;
        private String unCheckTextColor;
        private boolean isChecked;
        private String cornerMark;
        private float cornerMarkTextSize;
        private float cornerMarkPaddingVertical;
        private float cornerMarkPaddingHorizontal;

        public int getFragmentIndex() {
            return fragmentIndex;
        }

        public void setFragmentIndex(int fragmentIndex) {
            this.fragmentIndex = fragmentIndex;
        }

        public boolean isBigImage() {
            return bigImage;
        }

        public void setBigImage(boolean bigImage) {
            this.bigImage = bigImage;
        }

        public int getImageWidth() {
            return imageWidth;
        }

        public void setImageWidth(int imageWidth) {
            this.imageWidth = imageWidth;
        }

        public int getImageHeight() {
            return imageHeight;
        }

        public void setImageHeight(int imageHeight) {
            this.imageHeight = imageHeight;
        }

        public String getPageUrl() {
            return pageUrl;
        }

        public void setPageUrl(String pageUrl) {
            this.pageUrl = pageUrl;
        }

        public String getCheckedUrl() {
            return checkedUrl;
        }

        public void setCheckedUrl(String checkedUrl) {
            this.checkedUrl = checkedUrl;
        }

        public String getUnCheckedUrl() {
            return unCheckedUrl;
        }

        public void setUnCheckedUrl(String unCheckedUrl) {
            this.unCheckedUrl = unCheckedUrl;
        }

        public float getWeight() {
            return weight;
        }

        public void setWeight(float weight) {
            this.weight = weight;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getModuleName() {
            return moduleName;
        }

        public void setModuleName(String moduleName) {
            this.moduleName = moduleName;
        }

        public int getBetweenImageAndText() {
            return betweenImageAndText;
        }

        public void setBetweenImageAndText(int betweenImageAndText) {
            this.betweenImageAndText = betweenImageAndText;
        }

        public int getTextOffsetY() {
            return textOffsetY;
        }

        public void setTextOffsetY(int textOffsetY) {
            this.textOffsetY = textOffsetY;
        }

        public int getTextSize() {
            return textSize;
        }

        public void setTextSize(int textSize) {
            this.textSize = textSize;
        }

        public String getCheckTextColor() {
            return checkTextColor;
        }

        public void setCheckTextColor(String checkTextColor) {
            this.checkTextColor = checkTextColor;
        }

        public String getUnCheckTextColor() {
            return unCheckTextColor;
        }

        public void setUnCheckTextColor(String unCheckTextColor) {
            this.unCheckTextColor = unCheckTextColor;
        }

        public boolean isIsChecked() {
            return isChecked;
        }

        public void setIsChecked(boolean isChecked) {
            this.isChecked = isChecked;
        }

        public String getCornerMark() {
            return cornerMark;
        }

        public void setCornerMark(String cornerMark) {
            this.cornerMark = cornerMark;
        }

        public float getCornerMarkTextSize() {
            return cornerMarkTextSize;
        }

        public void setCornerMarkTextSize(float cornerMarkTextSize) {
            this.cornerMarkTextSize = cornerMarkTextSize;
        }

        public float getCornerMarkPaddingVertical() {
            return cornerMarkPaddingVertical;
        }

        public void setCornerMarkPaddingVertical(float cornerMarkPaddingVertical) {
            this.cornerMarkPaddingVertical = cornerMarkPaddingVertical;
        }

        public float getCornerMarkPaddingHorizontal() {
            return cornerMarkPaddingHorizontal;
        }

        public void setCornerMarkPaddingHorizontal(float cornerMarkPaddingHorizontal) {
            this.cornerMarkPaddingHorizontal = cornerMarkPaddingHorizontal;
        }
    }
}
