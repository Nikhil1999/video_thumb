import 'dart:convert';
import 'dart:io';

import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'model/video_meta_model.dart';
import 'video_thumb_platform_interface.dart';

/// An implementation of [VideoThumbPlatform] that uses method channels.
class MethodChannelVideoThumb extends VideoThumbPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel =
      const MethodChannel('in.lazymanstudios.videothumb/helper');

  @override
  Future<File> getFileThumbnail({required File file}) async {
    String path = await methodChannel.invokeMethod('getFileThumbnail', {
      "path": file.path,
    });
    return File(path);
  }

  @override
  Future<VideoMetaModel> getVideoMeta({required File file}) async {
    String json = await methodChannel.invokeMethod('getVideoMeta', {
      "path": file.path,
    });

    return VideoMetaModel.fromJson(jsonDecode(json));
  }
}
