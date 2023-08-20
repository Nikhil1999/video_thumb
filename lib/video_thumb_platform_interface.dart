import 'dart:io';

import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'model/video_meta_model.dart';
import 'video_thumb_method_channel.dart';

abstract class VideoThumbPlatform extends PlatformInterface {
  /// Constructs a VideoThumbPlatform.
  VideoThumbPlatform() : super(token: _token);

  static final Object _token = Object();

  static VideoThumbPlatform _instance = MethodChannelVideoThumb();

  /// The default instance of [VideoThumbPlatform] to use.
  ///
  /// Defaults to [MethodChannelVideoThumb].
  static VideoThumbPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [VideoThumbPlatform] when
  /// they register themselves.
  static set instance(VideoThumbPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<String?> getPlatformVersion() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }

  Future<File> getFileThumbnail({required File file}) async {
    throw UnimplementedError('getFileThumbnail() has not been implemented.');
  }

  Future<VideoMetaModel> getVideoMeta({required File file}) {
    throw UnimplementedError('getVideoMeta() has not been implemented.');
  }
}
