import { Component, OnInit } from '@angular/core';


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

  constructor() { }

  ngOnInit() {
  }

  onSubmit(form: any): void {
  	console.log('values:', form);
  	this.searchResult = [
  		{
  			'id': 'Cambiado', 
  			'name': 'Cambiado Name'
  		},
  		{
  			'id': 'Cambiado2', 
  			'name': 'Cambiado Name 2'
  		}
  	];
  }

}


