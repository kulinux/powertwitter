import { Injectable } from '@angular/core';
import * as Rx from 'rxjs/Rx';

@Injectable()
export class WebsocketService {



  connection: WebSocket;

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

    // Log messages from the server
    this.connection.onmessage = function (e) {
      console.log('Server: ' + e.data);
    };
  }

}
