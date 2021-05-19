import 'dart:async';
import 'dart:convert';
import 'dart:io';

import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter/services.dart';
import 'package:hello_matrix_flutter/hello_matrix_flutter.dart';
import 'package:hello_matrix_flutter_example/image_sender.dart';

//import 'package:image_size_getter/file_input.dart';
//import 'package:image_size_getter/image_size_getter.dart';
import 'package:path/path.dart';

//import 'package:image_size_getter/image_size_getter.dart';
//import 'package:storage_path/storage_path.dart';

class RoomDetails extends StatefulWidget {
  final String roomId;

  const RoomDetails({Key key, this.roomId}) : super(key: key);

  @override
  _RoomDetailsState createState() => _RoomDetailsState();
}

class _RoomDetailsState extends State<RoomDetails> {
  final _textCtl = TextEditingController();
  final _scrollCtrl = ScrollController();
  ValueNotifier<bool> _scrollButtonShowEvent = ValueNotifier<bool>(false);
  Timer _debounce;
  bool _typing = false;

  @override
  void initState() {
    init();
    paginate();
    super.initState();
    // Setup the listener.
    _scrollCtrl.addListener(() {
      if (_scrollCtrl.position.pixels > 1200) {
        _scrollButtonShowEvent.value = true;
      } else {
        _scrollButtonShowEvent.value = false;
      }

      if (_scrollCtrl.position.atEdge) {
        if (_scrollCtrl.position.pixels == 0) {
          print(' bottom  reached');
        } else {
          print(' top reached');
          paginate();
        }
      }
    });
    TimeLine.liveTimeLineTypingUsers.listen((event) {
      print('event $event');
    });
  }

  void paginate() async {
    bool result = await TimeLine.paginateBackward();
    print('paginate result $result');
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text("Chat"),
      ),
      body: Container(
        margin: EdgeInsets.all(8),
        child: Column(
          children: [
            Expanded(
              child: Stack(
                children: [
                  StreamBuilder(
                    stream: TimeLine.liveTimeLine,
                    builder: (BuildContext context,
                        AsyncSnapshot<dynamic> snapshot) {
                      if (snapshot == null || !snapshot.hasData)
                        return Container();
                      List<dynamic> messages = json.decode(snapshot.data);
                      return ListView.builder(
                        controller: _scrollCtrl,
                        itemCount: messages.length,
                        shrinkWrap: true,
                        padding: EdgeInsets.only(top: 10, bottom: 10),
                        reverse: true,
                        itemBuilder: (context, index) {
                          var message = messages[index];
                          String root = messages[index]['clearedContent'];

                          if (root == null) {
                            return Container();
                          }

                          //String type = messages[index]['clearedContent']['msgType'];
                          var decodedClearContent =
                              json.decode(messages[index]['clearedContent']);

                          String type = decodedClearContent['msgtype'];

                          String localImg = decodedClearContent['local'];
                          String fullImg = decodedClearContent['full'];
                          String thumbImg = decodedClearContent['thumb'];

                          return root != null
                              ? Container(
                                  padding: EdgeInsets.only(
                                      left: 14, right: 14, top: 10, bottom: 10),
                                  child: Align(
                                    alignment:
                                        (message['direction'] == "received"
                                            ? Alignment.topLeft
                                            : Alignment.topRight),
                                    child: Container(
                                      decoration: BoxDecoration(
                                        borderRadius: BorderRadius.circular(20),
                                        color:
                                            (message['direction'] == "received"
                                                ? Colors.grey.shade200
                                                : Colors.blue[200]),
                                      ),
                                      padding: EdgeInsets.all(16),
                                      child: type == "m.text"
                                          ? Text(
                                              json
                                                  .decode(root)['body']
                                                  .toString(),
                                              style: TextStyle(fontSize: 15),
                                            )
                                          : Container(
                                              width: 100,
                                              height: 100,
                                              child: localImg != null
                                                  ? Image.file(
                                                      File(localImg),
                                                      fit: BoxFit.cover,
                                                    )
                                                  : Image.network(
                                                      thumbImg,
                                                      fit: BoxFit.cover,
                                                    ),
                                            ),
                                    ),
                                  ),
                                )
                              : Container();
                        },
                      );
                    },
                  ),
                  Positioned(
                    right: 30,
                    bottom: 100,
                    child: ValueListenableBuilder(
                        valueListenable: _scrollButtonShowEvent,
                        builder: (context, value, child) {
                          if (value) {
                            return IconButton(
                              onPressed: () {
                                _scrollCtrl.animateTo(
                                  0.0,
                                  curve: Curves.easeOut,
                                  duration: const Duration(milliseconds: 300),
                                );
                              },
                              icon: Icon(
                                Icons.arrow_drop_down_circle_outlined,
                                size: 30,
                              ),
                            );
                          } else {
                            return Container(
                              height: 0,
                              width: 0,
                            );
                          }
                        }),
                  )
                ],
              ),
            ),
            StreamBuilder<List<String>>(
                stream: TimeLine.liveTimeLineTypingUsers,
                builder: (context, snapshot) {
                  print(snapshot.data);
                  if (snapshot.hasData &&
                      snapshot.data != null &&
                      snapshot.data.isNotEmpty) {
                    return Text(
                      'Typing',
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis,
                      style: TextStyle(),
                    );
                  } else {
                    return Container(
                      height: 0,
                      width: 0,
                    );
                  }
                }),
            Row(
              mainAxisSize: MainAxisSize.min,
              children: [
                Expanded(
                    child: TextField(
                  onChanged: (val) {
                    if (!_typing) {
                      _typing = true;
                      TimeLine.onStartTyping();
                    }

                    if (_debounce?.isActive ?? false) _debounce.cancel();
                    _debounce = Timer(const Duration(milliseconds: 500), () {
                      TimeLine.onStopTyping();
                      _typing = false;
                    });
                  },
                  controller: _textCtl,
                )),
                SizedBox(
                  width: 5,
                ),
                IconButton(
                  icon: Icon(Icons.photo),
                  onPressed: () async {
                    /*TimeLine.sendSingleImageMessage(
                        widget.roomId,
                        'Simple body',
                        '/storage/emulated/0/DCIM/Important Pictures/IMG-20180813-WA0007.jpg');*/

                    ContentAttachmentMedia m1 = ContentAttachmentMedia();
                    ContentAttachmentMedia m2 = ContentAttachmentMedia();

                    m1.body = "SB1";
                    m1.path = "/storage/emulated/0/DCIM/Important Pictures/IMG-20180813-WA0007.jpg";

                    m2.body = "SB2";
                    m2.path = "/storage/emulated/0/DCIM/Important Pictures/IMG-20180813-WA0007.jpg";

                    TimeLine.sendMultipleImageMessage( widget.roomId,[
                      m1,m2
                    ]);
                    /* Navigator.push(
                      context,
                      MaterialPageRoute(builder: (context) => ImageSender()),
                    );*/
                  },
                ),
                ElevatedButton(
                    onPressed: () async {
                      await TimeLine.sendSimpleTextMessage(
                          widget.roomId, _textCtl.text);
                      _textCtl.clear();
                    },
                    child: Text('Send'))
              ],
            )
          ],
        ),
      ),
    );
  }

  init() async {
    await TimeLine.createTimeLine(widget.roomId);
    TimeLine.liveTimeLine.listen((event) {
      //print(event);
    });
  }

  @override
  void dispose() {
    // Clean up the controller when the widget is disposed.
    TimeLine.onStopTyping();
    _textCtl.dispose();
    _scrollCtrl.dispose();
    _debounce?.cancel();
    TimeLine.destroyTimeLine();
    super.dispose();
  }
}
