package com.innovathon.sideways.util;

import android.content.Context;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.widget.FrameLayout;

public class TouchableWrapper extends FrameLayout {

    private static final long SCROLL_TIME = 200L; // 200 Milliseconds, but you can adjust that to your liking
    private long lastTouched = 0;
    private UpdateMapAfterUserInterection updateMapAfterUserInterection;
    private OnMapClicked onMapClicked;

    public TouchableWrapper(Context context) {
        super(context);
        // Force the host activity to implement the UpdateMapAfterUserInterection Interface
        try {
            updateMapAfterUserInterection = (com.innovathon.sideways.main.MainActivity) context;
            onMapClicked = (com.innovathon.sideways.main.MainActivity) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement UpdateMapAfterUserInterection");
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastTouched = SystemClock.uptimeMillis();
                break;

            case MotionEvent.ACTION_UP:
                final long now = SystemClock.uptimeMillis();
                if (now - lastTouched > SCROLL_TIME) {
                    // Update the map
                    updateMapAfterUserInterection.onUpdateMapAfterUserInterection();
                } else {
                    if (onMapClicked != null)
                        onMapClicked.onMapClicked();
                }
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    // Map Activity must implement this interface
    public interface UpdateMapAfterUserInterection {
        void onUpdateMapAfterUserInterection();
    }

    public interface OnMapClicked {
        void onMapClicked();
    }
}