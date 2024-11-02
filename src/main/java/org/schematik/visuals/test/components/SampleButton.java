package org.schematik.visuals.test.components;

import org.schematik.visuals.component.atomic.AbstractButtonComponent;
import org.schematik.visuals.component.atomic.TextComponent;

public class SampleButton extends AbstractButtonComponent {
    int clickedNumber;

    public SampleButton(String state) {

    }

    @Override
    public void onClick() {
        clickedNumber++;
        getText().setText(String.format("Clicked %d time(s)", clickedNumber));
    }

    public int getClickedNumber() {
        return clickedNumber;
    }

    public void setClickedNumber(int clickedNumber) {
        this.clickedNumber = clickedNumber;
    }
}
