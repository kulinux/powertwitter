import { Component, OnInit } from '@angular/core';
import {Http, Response} from '@angular/http';

@Component({
  selector: 'app-put',
  templateUrl: './put.component.html',
  styleUrls: ['./put.component.css']
})
export class PutComponent implements OnInit {

  constructor(private http: Http) { }

  ngOnInit() {
  }

  onSubmit(form: any): void {
  	console.log('values:', form);
  	this.makeRequest(form);
  }

  makeRequest(form) {
  	this.http.put(
  		'http://localhost:4200/twitter/tweets/1',
  		JSON.stringify('kkkkkkk')
  	)
  	.subscribe((res: Response) => {
  		//this.data = res.json();
  		console.log('response ', res);
  	});
  }
}
