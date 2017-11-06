import { Injectable } from '@angular/core';
import * as Rx from 'rxjs/Rx';

export interface TwitterData {
  id: string;
  tweet: string;
  metadata: string
}

@Injectable()
export class WebsocketService {

  connection: WebSocket;
  subject = new Rx.Subject<TwitterData>();

  constructor() {
    let WS_URL = "http://localhost:8080/ws";
    this.connect(WS_URL);
  }

  connect(url): void {
    this.connection = new WebSocket("ws://localhost:9000/ws")
    // When the connection is open, send some data to the server
    var that = this;
    this.connection.onopen = function () {
      that.connection.send('Ping'); // Send the message 'Ping' to the server
    };

    // Log errors
    this.connection.onerror = function (error) {
      console.log('WebSocket Error ' + error);
    };

    var that = this;

    // Log messages from the server
    this.connection.onmessage = function (e) {
      console.log('Server: ' + e.data);
      let json = JSON.parse(e.data);
      that.subject.next(
        JSON.parse('{ "id":'+ '"' + json.id + '",' +
        '"name":"'+ json.tweet + '",' +
        '"metadata":"'+ json.metadata + '"' +
        '}'
      )
      );
    };
  }

}
