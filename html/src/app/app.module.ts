import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';

import { AppComponent } from './app.component';
import { PutComponent } from './put/put.component';
import { SearchComponent } from './search/search.component';

import { HttpModule } from '@angular/http';

import {
  FormsModule,
  ReactiveFormsModule
} from '@angular/forms'

@NgModule({
  declarations: [
    AppComponent,
    PutComponent,
    SearchComponent
  ],
  imports: [
    BrowserModule,
    FormsModule,
    ReactiveFormsModule,
    HttpModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
