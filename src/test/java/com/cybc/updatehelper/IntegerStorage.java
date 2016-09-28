package com.cybc.updatehelper;

import java.util.ArrayList;

public class IntegerStorage extends ArrayList<Integer> {

    private boolean closed;

    //much wow, storage in ram -> fast
    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    public boolean isClosed() {
        return closed;
    }
}
