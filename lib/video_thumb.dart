import 'dart:io';

import 'video_thumb_platform_interface.dart';

class VideoThumb {
  Future<String?> getPlatformVersion() {
    return VideoThumbPlatform.instance.getPlatformVersion();
  }

  static Future<File> getFileThumbnail({required File file}) {
    return VideoThumbPlatform.instance.getFileThumbnail(file: file);
  }
}
