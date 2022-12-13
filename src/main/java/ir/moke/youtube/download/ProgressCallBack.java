package ir.moke.youtube.download;

import java.nio.channels.ReadableByteChannel;

public interface ProgressCallBack {
    void callBack(ReadableByteChannel readableByteChannel, long sizeRead, long progress);
}
