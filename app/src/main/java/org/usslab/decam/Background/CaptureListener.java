package org.usslab.decam.Background;

import org.usslab.decam.Data.Packet;

/**
 * Created by pip on 2017/1/30.
 */

public interface CaptureListener {
    void onStart(String currentFilename);
    void onStop();
    void onFail();
    void onProgress(Packet aPacket);
}
