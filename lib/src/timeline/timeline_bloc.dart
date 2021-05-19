import 'dart:convert';

import 'package:flutter/services.dart';
import 'package:hello_matrix_flutter/src/models/content_attachment_media.dart';

const MethodChannel _channel = const MethodChannel('hello_matrix_flutter');

const EventChannel _channelTimelineEvents =
    const EventChannel('hello_matrix_flutter/timelineEvents');

const EventChannel _channelTimelineTypingUsersEvents =
    const EventChannel('hello_matrix_flutter/typingUsersEvents');

class TimeLine {
  static Future<bool> sendSimpleTextMessage(String roomId, String body) async {
    final bool result = await _channel.invokeMethod("sendSimpleTextMessage", {
      'roomId': roomId,
      'body': body,
    });
    return result;
  }

  //@todo change to attachment media data class
  static Future<String> sendSingleImageMessage(String roomId,String body,String path) async {
    final String result =
    await _channel.invokeMethod("sendSingleImageMessage", {'roomId': roomId,
      'body': body,
      'path': path,
    });
    return result;
  }

  static Future<String> sendMultipleImageMessage(String roomId,List<ContentAttachmentMedia> medias) async {

    //convert to json
    String jsonConverted = json.encode(medias);
    final String result =
    await _channel.invokeMethod("sendMultipleImageMessage", {'roomId': roomId,
      'jsonData': jsonConverted,
    });
    return result;
  }

  static Future<bool> onStartTyping() async {
    bool result = await _channel.invokeMethod("onStartTyping");
    return result;
  }

  static Future<bool> onStopTyping() async {
    bool result = await _channel.invokeMethod("onStopTyping");
    return result;
  }

  static Future<void> destroyTimeLine() async {
    await _channel.invokeMethod("destroyTimeLine");
    return;
  }

  static Future<void> createTimeLine(String roomId) async {
    await _channel.invokeMethod("createTimeLine", {'roomId': roomId});
    return;
  }

  static Future<bool> paginateBackward() async {
    return await _channel.invokeMethod('paginateBackward');
  }

  static Stream get liveTimeLine =>
      _channelTimelineEvents.receiveBroadcastStream();

  static Stream<List<String>> get liveTimeLineTypingUsers =>
      _channelTimelineTypingUsersEvents
          .receiveBroadcastStream()
          .asyncMap((event) {
        if (event != null) {
          List<String> users = [];
          for (var i in event) {
            users.add(i.toString());
          }
          return users;
        } else {
          return [];
        }
      });
}
