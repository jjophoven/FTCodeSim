package org.codeblooded.ftcodesim.driverstation.client.ui;

import org.codeblooded.ftcodesim.driverstation.client.DSClientModel;
import org.codeblooded.ftcodesim.driverstation.client.ui.components.*;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class DSClientView extends JPanel {

    private final DSClientModel clientModel;

    private final DSComponentStyle componentStyle;
    private final DSComponentLayout componentLayout;

    private ConnectionLabel connectionLabel;
    private OpModeSelector opModeSelector;
    private OpModeStateLabel opModeStateLabel;
    private OpModeControlButtons opModeControlButtons;
    private TelemetryArea telemetryArea;
    private TimerLabel timerLabel;

    private final Consumer<Boolean> connectionCallback;

    public DSClientView(int port, Consumer<Boolean> connectionCallback, DSComponentStyle componentStyle, DSComponentLayout componentLayout) {
        super();
        this.connectionCallback = connectionCallback;
        this.componentStyle = componentStyle;
        this.componentLayout = componentLayout;

        initUIComponents();

        this.clientModel = new DSClientModel(port,
                this::onConnection,
                this.opModeSelector::updateAvailableOpModes,
                (state) -> {
                    this.opModeStateLabel.changeIndicatorState(state);
                    this.opModeControlButtons.onOpModeStateChanged(state);
                    },
                this.telemetryArea::updateTelemetry,
                this.timerLabel::updateTime,
                (i) -> {},
                (i) -> {}
        );

        this.opModeSelector.acceptClient(this.clientModel);
        this.opModeControlButtons.acceptClient(this.clientModel);

        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(this.clientModel);
    }

    public DSClientView(int port, Consumer<Boolean> connectionCallback) {
        this(port, connectionCallback, new DSComponentStyle(), new DSComponentLayout());
    }

    private void initUIComponents(){
        this.connectionLabel = new ConnectionLabel();
        this.opModeSelector = new OpModeSelector();
        this.opModeStateLabel = new OpModeStateLabel();
        this.opModeControlButtons = new OpModeControlButtons();
        this.telemetryArea = new TelemetryArea();
        this.timerLabel = new TimerLabel();

        this.connectionLabel.setPreferredSize(new Dimension(100,25));

        this.componentStyle.styleConnectionLabel(this.connectionLabel);
        this.componentStyle.styleOpModeSelector(this.opModeSelector);
        this.componentStyle.styleOpModeStateLabel(this.opModeStateLabel);
        this.componentStyle.styleOpModeControlButtons(this.opModeControlButtons);
        this.componentStyle.styleTelemetryArea(this.telemetryArea);
        this.componentStyle.styleTimerLabel(this.timerLabel);

        this.componentStyle.styleWindow(this);
        this.componentLayout.mainLayout(
                this.componentStyle,
                this,
                this.connectionLabel,
                this.opModeSelector,
                this.opModeStateLabel,
                this.opModeControlButtons,
                this.timerLabel,
                this.telemetryArea);
    }


    private void onConnection(boolean isConnected){
        this.connectionLabel.onConnectionStateChange(isConnected);
        this.connectionCallback.accept(isConnected);
    }

    public DSClientModel getClientModel() {
        return clientModel;
    }
}
