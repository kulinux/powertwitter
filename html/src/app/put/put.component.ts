import { Component, OnInit } from '@angular/core';
import {Http, Response, Headers} from '@angular/http';

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
    console.log(form);

    let header = new Headers({'Content-Type': 'application/json'});


  	this.http.put(
  		'http://localhost:9000/tweets',
  		JSON.stringify(form),
      {headers: header}
  	)
  	.subscribe((res: Response) => {
  		//this.data = res.json();
  		console.log('response ', res);
  	});
  }
}
