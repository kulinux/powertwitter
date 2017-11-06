import { Component, OnInit } from '@angular/core';
import { Injectable } from '@angular/core';
import { Observable, Subject } from 'rxjs/Rx';
import { WebsocketService, TwitterData } from '../websocket.service';


@Component({
  selector: 'app-result',
  templateUrl: './result.component.html',
  styleUrls: ['./result.component.css']
})
export class ResultComponent implements OnInit {


  searchResult : Array<TwitterData> = [];


  constructor(wsService: WebsocketService) {
    wsService.subject.subscribe( x => {
      console.log(x)
      this.searchResult.push( x );
    }
    );
  }

  ngOnInit() {
  }

}
