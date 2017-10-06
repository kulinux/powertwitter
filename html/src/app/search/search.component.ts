import { Component, OnInit } from '@angular/core';
import {Http, Response, Headers} from '@angular/http';



interface SearchResultItem {
	id: string;
	name: string;
}

@Component({
  selector: 'app-search',
  templateUrl: './search.component.html',
  styleUrls: ['./search.component.css']
})
export class SearchComponent implements OnInit {

  searchResult : Array<SearchResultItem> = [];

  constructor(private http: Http) { }

  ngOnInit() {
  }

  onSubmit(form: any): void {
  	console.log('values:', form);

    let header = new Headers({'Content-Type': 'application/json'});

    this.http.get(
      'http://localhost:9000/tweets',
      {headers: header}
    )
    .subscribe((res: Response) => {
      //this.data = res.json();
      this.searchResult = res.json().map( x => 
        JSON.parse('{ "id":'+ '"' + x.id + '",' +
        '"name":"'+ x.tweet + '"' + 
        '}')
      );
    });

  }

}


