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
  Future<File> getThumbnailFromFile({required File file}) async {
    String path = await methodChannel.invokeMethod('getFileThumbnail', {
      "path": file.path,
    });
    return File(path);
  }

  @override
  Future<File> getThumbnailFromUri({required String uri}) async {
    String path = await methodChannel.invokeMethod('getUriThumbnail', {
      "uri": uri,
    });
    return File(path);
  }

  @override
  Future<String> getFileNameFromUri({required String uri}) async {
    String fileName = await methodChannel.invokeMethod('getUriFileName', {
      "uri": uri,
    });
    return fileName;
  }

  @override
  Future<VideoMetaModel> getVideoMeta({required File file}) async {
    String json = await methodChannel.invokeMethod('getVideoMeta', {
      "path": file.path,
    });

    return VideoMetaModel.fromJson(jsonDecode(json));
  }

  @override
  Future<void> clearCache() async {
    await methodChannel.invokeMethod('clearTemporaryFiles');
  }
}
