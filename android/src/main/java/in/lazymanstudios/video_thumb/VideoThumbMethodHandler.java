package in.lazymanstudios.video_thumb;

import android.content.Context;

import androidx.annotation.NonNull;
import in.lazymanstudios.video_thumb.model.MethodResultWrapper;
import in.lazymanstudios.video_thumb.model.VideoThumb;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;

public class VideoThumbMethodHandler implements MethodChannel.MethodCallHandler {
    private final VideoThumb model;

    public VideoThumbMethodHandler(Context context) {
        model = new VideoThumb(context);
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull MethodChannel.Result result) {
        switch (call.method) {
            case "getFileThumbnail": {
                String path = call.argument("path");
                model.getFileThumbnail(new MethodResultWrapper(result), path);
                break;
            }
            case "getUriThumbnail": {
                String uri = call.argument("uri");
                model.getUriThumbnail(new MethodResultWrapper(result), uri);
                break;
            }
            case "getVideoMeta": {
                String path = call.argument("path");
                model.getVideoMeta(new MethodResultWrapper(result), path);
                break;
            }
            case "clearTemporaryFiles": {
                model.clearTemporaryFiles(new MethodResultWrapper(result));
                break;
            }
            default: {
                result.notImplemented();
                break;
            }
        }
    }
}
