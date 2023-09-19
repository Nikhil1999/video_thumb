import 'dart:io';

import 'model/video_meta_model.dart';
import 'video_thumb_platform_interface.dart';

export 'model/video_meta_model.dart';

class VideoThumb {
  Future<String?> getPlatformVersion() {
    return VideoThumbPlatform.instance.getPlatformVersion();
  }

  static Future<File> getThumbnailFromFile({required File file}) {
    return VideoThumbPlatform.instance.getThumbnailFromFile(file: file);
  }

  static Future<File> getThumbnailFromUri({required String uri}) {
    return VideoThumbPlatform.instance.getThumbnailFromUri(uri: uri);
  }

  static Future<VideoMetaModel> getVideoMeta({required File file}) {
    return VideoThumbPlatform.instance.getVideoMeta(file: file);
  }

  static Future<void> clearCache() {
    return VideoThumbPlatform.instance.clearCache();
  }
}
