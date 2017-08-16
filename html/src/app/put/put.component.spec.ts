import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { PutComponent } from './put.component';

describe('PutComponent', () => {
  let component: PutComponent;
  let fixture: ComponentFixture<PutComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ PutComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PutComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
