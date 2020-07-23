package com.jrda.ws_authentication.dao.document;

public class Check {
    private boolean checked;
    private String text;

    public Check() {
    }

    public Check(boolean checked, String text) {
        this.checked = checked;
        this.text = text;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
