package com.ramussoft.chart.core;

import com.ramussoft.common.Element;

public class ChartBounds {

    private double x;

    private double y;

    private double width;

    private double height;

    private Element chart;

    private Element link;

    public ChartBounds() {
    }

    public ChartBounds(double x, double y, double width, double height,
                       Element chart, Element link) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.chart = chart;
        this.setLink(link);
    }

    /**
     * @param x the x to set
     */
    public void setX(double x) {
        this.x = x;
    }

    /**
     * @return the x
     */
    public double getX() {
        return x;
    }

    /**
     * @param y the y to set
     */
    public void setY(double y) {
        this.y = y;
    }

    /**
     * @return the y
     */
    public double getY() {
        return y;
    }

    /**
     * @param width the width to set
     */
    public void setWidth(double width) {
        this.width = width;
    }

    /**
     * @return the width
     */
    public double getWidth() {
        return width;
    }

    /**
     * @param height the height to set
     */
    public void setHeight(double height) {
        this.height = height;
    }

    /**
     * @return the height
     */
    public double getHeight() {
        return height;
    }

    /**
     * @param chart the chart to set
     */
    public void setChart(Element chart) {
        this.chart = chart;
    }

    /**
     * @return the chart
     */
    public Element getChart() {
        return chart;
    }

    /**
     * @param link the link to set
     */
    public void setLink(Element link) {
        this.link = link;
    }

    /**
     * @return the link
     */
    public Element getLink() {
        return link;
    }

}
