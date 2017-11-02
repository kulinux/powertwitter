import { Component, OnInit } from '@angular/core';
import { Injectable } from '@angular/core';
import { Observable, Subject } from 'rxjs/Rx';
import { WebsocketService } from '../websocket.service';

export interface TwitterData {
  id: string;
  tweet: string;
  metadata: string
}

@Component({
  selector: 'app-result',
  templateUrl: './result.component.html',
  styleUrls: ['./result.component.css']
})
export class ResultComponent implements OnInit {


  constructor(wsService: WebsocketService) {
  }

  ngOnInit() {
  }

}
