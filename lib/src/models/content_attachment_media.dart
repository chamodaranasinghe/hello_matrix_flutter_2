enum MediaType{Image, Video, Voice, File}

class ContentAttachmentMedia{
  String body;
  String path;
  MediaType mediaType;

  Map toJson() => {
    'body': body,
    'path': path,
    'mediaType':mediaType
  };
}