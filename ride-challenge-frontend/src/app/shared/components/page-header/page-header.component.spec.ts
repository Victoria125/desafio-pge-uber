import { ComponentFixture, TestBed } from '@angular/core/testing';
import { PageHeaderComponent } from './page-header.component';

describe('PageHeaderComponent', () => {
  let fixture: ComponentFixture<PageHeaderComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PageHeaderComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(PageHeaderComponent);
  });

  it('should render the title', () => {
    fixture.componentRef.setInput('title', 'Minhas corridas');
    fixture.detectChanges();

    const heading = (fixture.nativeElement as HTMLElement).querySelector('h1');
    expect(heading?.textContent).toContain('Minhas corridas');
  });

  it('should render the subtitle only when provided', () => {
    fixture.componentRef.setInput('title', 'Minhas corridas');
    fixture.detectChanges();

    let subtitle = (fixture.nativeElement as HTMLElement).querySelector('.page-subtitle');
    expect(subtitle).toBeNull();

    fixture.componentRef.setInput('subtitle', 'Ana Cliente');
    fixture.detectChanges();

    subtitle = (fixture.nativeElement as HTMLElement).querySelector('.page-subtitle');
    expect(subtitle?.textContent).toContain('Ana Cliente');
  });
});
