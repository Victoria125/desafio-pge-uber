import { ComponentFixture, TestBed } from '@angular/core/testing';
import { providePrimeNG } from 'primeng/config';
import { RideStatusTagComponent } from './ride-status-tag.component';

describe('RideStatusTagComponent', () => {
  let fixture: ComponentFixture<RideStatusTagComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RideStatusTagComponent],
      providers: [providePrimeNG()],
    }).compileComponents();

    fixture = TestBed.createComponent(RideStatusTagComponent);
  });

  it('should label a created ride from the client perspective', () => {
    fixture.componentRef.setInput('status', 'CREATED');
    fixture.componentRef.setInput('perspective', 'client');
    fixture.detectChanges();

    const text = (fixture.nativeElement as HTMLElement).textContent ?? '';
    expect(text).toContain('Aguardando motorista');
  });

  it('should label a created ride from the driver perspective', () => {
    fixture.componentRef.setInput('status', 'CREATED');
    fixture.componentRef.setInput('perspective', 'driver');
    fixture.detectChanges();

    const text = (fixture.nativeElement as HTMLElement).textContent ?? '';
    expect(text).toContain('Disponível');
  });
});
