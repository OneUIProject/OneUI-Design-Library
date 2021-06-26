package com.samsung.android.sdk.pen.settingui.colorpicker;

import java.util.ArrayList;
import java.util.List;

/* access modifiers changed from: package-private */
public class SpenPickerColorEventManager {
    List<SpenPickerColorEventListener> mListeners = new ArrayList();

    public void close() {
        if (this.mListeners != null) {
            this.mListeners = null;
        }
    }

    public void subscribe(SpenPickerColorEventListener spenPickerColorEventListener) {
        List<SpenPickerColorEventListener> list = this.mListeners;
        if (list != null) {
            list.add(spenPickerColorEventListener);
        }
    }

    public void unsubscribe(SpenPickerColorEventListener spenPickerColorEventListener) {
        List<SpenPickerColorEventListener> list = this.mListeners;
        if (list != null) {
            list.remove(spenPickerColorEventListener);
        }
    }

    public void notify(String str, int i, float[] fArr) {
        for (SpenPickerColorEventListener spenPickerColorEventListener : this.mListeners) {
            spenPickerColorEventListener.update(str, i, fArr[0], fArr[1], fArr[2]);
        }
    }
}
