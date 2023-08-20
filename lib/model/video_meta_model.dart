class VideoMetaModel {
  final String? duration;

  const VideoMetaModel({
    required this.duration,
  });

  factory VideoMetaModel.fromJson(Map<String, dynamic> json) {
    return VideoMetaModel(
      duration: json['duration'],
    );
  }

  Map<String, dynamic> toJson() => {'duration': duration};
}
