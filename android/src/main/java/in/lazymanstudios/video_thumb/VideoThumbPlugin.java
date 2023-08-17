package in.lazymanstudios.video_thumb;

import android.content.Context;

import androidx.annotation.NonNull;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodChannel;

/** VideoThumbPlugin */
public class VideoThumbPlugin implements FlutterPlugin {
  private static final String METHOD_CHANNEL = "in.lazymanstudios.videothumb/helper";

  private MethodChannel methodChannel;

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    setupMethodChannel(flutterPluginBinding.getApplicationContext(), flutterPluginBinding.getBinaryMessenger());
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    destoryMethodChannel();
  }

  void setupMethodChannel(Context context, BinaryMessenger binaryMessenger) {
    methodChannel = new MethodChannel(binaryMessenger, METHOD_CHANNEL);
    VideoThumbMethodHandler methodCallHandler = new VideoThumbMethodHandler(context);
    methodChannel.setMethodCallHandler(methodCallHandler);
  }

  void destoryMethodChannel() {
    methodChannel.setMethodCallHandler(null);
    methodChannel = null;
  }
}
