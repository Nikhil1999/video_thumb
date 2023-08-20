class VideoMetaModel {
  final String? _durationStr;
  late final Duration? duration;

  VideoMetaModel({
    required String? duration,
  }) : _durationStr = duration {
    Duration? tempDuration;

    String? durationStr = _durationStr;
    if (durationStr != null) {
      int? durationInt = int.tryParse(durationStr);
      if (durationInt != null) {
        tempDuration = Duration(milliseconds: durationInt);
      }
    }

    this.duration = tempDuration;
  }

  factory VideoMetaModel.fromJson(Map<String, dynamic> json) {
    return VideoMetaModel(
      duration: json['duration'],
    );
  }

  Map<String, dynamic> toJson() => {
        'duration': _durationStr,
      };
}
