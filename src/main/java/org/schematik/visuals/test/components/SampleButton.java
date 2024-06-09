package org.schematik.visuals.test.components;

import org.schematik.visuals.component.atomic.AbstractButtonComponent;
import org.schematik.visuals.component.atomic.TextComponent;

public class SampleButton extends AbstractButtonComponent {
    int clickedNumber;

    @Override
    public void onClick() {
        clickedNumber++;
        TextComponent textComponent = new TextComponent();
        textComponent.setText(String.format("Clicked %d times", clickedNumber));
        getText();
    }

    public int getClickedNumber() {
        return clickedNumber;
    }

    public void setClickedNumber(int clickedNumber) {
        this.clickedNumber = clickedNumber;
    }
}
