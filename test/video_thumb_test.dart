import 'package:flutter_test/flutter_test.dart';
import 'package:video_thumb/video_thumb.dart';
import 'package:video_thumb/video_thumb_platform_interface.dart';
import 'package:video_thumb/video_thumb_method_channel.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockVideoThumbPlatform
    with MockPlatformInterfaceMixin
    implements VideoThumbPlatform {

  @override
  Future<String?> getPlatformVersion() => Future.value('42');
}

void main() {
  final VideoThumbPlatform initialPlatform = VideoThumbPlatform.instance;

  test('$MethodChannelVideoThumb is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelVideoThumb>());
  });

  test('getPlatformVersion', () async {
    VideoThumb videoThumbPlugin = VideoThumb();
    MockVideoThumbPlatform fakePlatform = MockVideoThumbPlatform();
    VideoThumbPlatform.instance = fakePlatform;

    expect(await videoThumbPlugin.getPlatformVersion(), '42');
  });
}
