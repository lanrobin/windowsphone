package com.axen.launcher.wp7.ui;

import android.view.View;
/**
 * Interface defining an object that can originate a drag.
 *
 */
public interface DragSource {
    void setDragController(DragController dragger);
    void onDropCompleted(View target, boolean success);
}
