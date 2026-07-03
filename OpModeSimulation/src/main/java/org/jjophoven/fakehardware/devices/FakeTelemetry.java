package org.jjophoven.fakehardware.devices;

import org.firstinspires.ftc.robotcore.external.Func;
import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.jjophoven.simulator.DriverStationSimulator;

public class FakeTelemetry implements Telemetry {
    private final DriverStationSimulator driverStation;

    public FakeTelemetry(DriverStationSimulator driverStation) {
        this.driverStation = driverStation;
    }

    @Override
    public Item addData(String caption, String format, Object... args) {
        return null;
    }

    @Override
    public Item addData(String caption, Object value) {
        return null;
    }

    @Override
    public <T> Item addData(String caption, Func<T> valueProducer) {
        return null;
    }

    @Override
    public <T> Item addData(String caption, String format, Func<T> valueProducer) {
        return null;
    }

    @Override
    public boolean removeItem(Item item) {
        return false;
    }

    @Override
    public void clear() {

    }

    @Override
    public void clearAll() {

    }

    @Override
    public Object addAction(Runnable action) {
        return null;
    }

    @Override
    public boolean removeAction(Object token) {
        return false;
    }

    @Override
    public void speak(String text) {

    }

    @Override
    public void speak(String text, String languageCode, String countryCode) {

    }

    String toBeDisplayed = "";

    @Override
    public boolean update() {
        driverStation.sendTelemetry(toBeDisplayed);
        toBeDisplayed = "";
        return false;
    }

    @Override
    public Line addLine() {
        return null;
    }

    @Override
    public Line addLine(String lineCaption) {
        toBeDisplayed += lineCaption + "\n";
        return null;
    }

    @Override
    public boolean removeLine(Line line) {
        return false;
    }

    @Override
    public boolean isAutoClear() {
        return false;
    }

    @Override
    public void setAutoClear(boolean autoClear) {

    }

    @Override
    public int getMsTransmissionInterval() {
        return 0;
    }

    @Override
    public void setMsTransmissionInterval(int msTransmissionInterval) {

    }

    @Override
    public String getItemSeparator() {
        return "";
    }

    @Override
    public void setItemSeparator(String itemSeparator) {

    }

    @Override
    public String getCaptionValueSeparator() {
        return "";
    }

    @Override
    public void setCaptionValueSeparator(String captionValueSeparator) {

    }

    @Override
    public void setDisplayFormat(DisplayFormat displayFormat) {

    }

    @Override
    public Log log() {
        return null;
    }
}
