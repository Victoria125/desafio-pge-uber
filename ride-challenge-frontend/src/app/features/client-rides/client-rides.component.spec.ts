import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormGroup } from '@angular/forms';
import { MessageService } from 'primeng/api';
import { providePrimeNG } from 'primeng/config';
import { of, throwError } from 'rxjs';
import type {
  AccountDto,
  CreateRideRequestDto,
  RideDto,
  UpdateRideRequestDto,
} from '../../core/api/api-dtos';
import { RideService } from '../../core/api/ride.service';
import { ClientRidesComponent } from './client-rides.component';

const STORAGE_KEY = 'ride-challenge.session';

const account: AccountDto = {
  id: 'client-1',
  name: 'Ana Cliente',
  email: 'ana@example.com',
  type: 'CLIENT',
};

const rides: RideDto[] = [
  {
    id: 'ride-1',
    userId: 'client-1',
    driverId: null,
    startAddress: 'Unifor',
    destinationAddress: 'Praia de Iracema',
    status: 'CREATED',
    createdAt: '2026-07-09T10:00:00Z',
  },
  {
    id: 'ride-2',
    userId: 'client-2',
    driverId: null,
    startAddress: 'Outro cliente',
    destinationAddress: 'Centro',
    status: 'CREATED',
    createdAt: '2026-07-09T11:00:00Z',
  },
];

interface ClientComponentInternals {
  createForm: FormGroup;
  editForm: FormGroup;
  submitCreate(): void;
  startEditingRide(ride: RideDto): void;
  saveEditingRide(ride: RideDto): void;
  canEditRide(ride: RideDto): boolean;
}

describe('ClientRidesComponent', () => {
  let fixture: ComponentFixture<ClientRidesComponent>;
  let rideService: jasmine.SpyObj<RideService>;

  beforeEach(async () => {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(account));
    rideService = jasmine.createSpyObj<RideService>('RideService', [
      'listRides',
      'createRide',
      'updateRide',
    ]);
    rideService.listRides.and.returnValue(of(rides));
    rideService.createRide.and.returnValue(of({ id: 'ride-new' }));
    rideService.updateRide.and.returnValue(
      of({
        ...rides[0],
        startAddress: 'Avenida A',
        destinationAddress: 'Rua B',
        updatedAt: '2026-07-09T10:05:00Z',
      })
    );

    await TestBed.configureTestingModule({
      imports: [ClientRidesComponent],
      providers: [
        MessageService,
        providePrimeNG(),
        { provide: RideService, useValue: rideService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ClientRidesComponent);
    fixture.detectChanges();
  });

  afterEach(() => {
    localStorage.clear();
  });

  it('should render only rides from the selected client', async () => {
    await fixture.whenStable();
    fixture.detectChanges();

    const text = (fixture.nativeElement as HTMLElement).textContent ?? '';
    expect(text).toContain('Unifor');
    expect(text).toContain('Praia de Iracema');
    expect(text).not.toContain('Outro cliente');
  });

  it('should create a ride with the selected client id', () => {
    const component = fixture.componentInstance as unknown as ClientComponentInternals;
    component.createForm.setValue({
      startAddress: 'Avenida A',
      destinationAddress: 'Rua B',
    });

    component.submitCreate();

    expect(rideService.createRide).toHaveBeenCalledWith({
      userId: 'client-1',
      startAddress: 'Avenida A',
      destinationAddress: 'Rua B',
    } satisfies CreateRideRequestDto);
  });

  it('should not call the API when the ride form is invalid', () => {
    const component = fixture.componentInstance as unknown as ClientComponentInternals;
    component.createForm.setValue({
      startAddress: '',
      destinationAddress: '',
    });

    component.submitCreate();

    expect(rideService.createRide).not.toHaveBeenCalled();
  });

  it('should keep the current list when ride creation fails', () => {
    const component = fixture.componentInstance as unknown as ClientComponentInternals;
    rideService.createRide.and.returnValue(
      throwError(() => new Error('Queue communication error'))
    );
    component.createForm.setValue({
      startAddress: 'Avenida A',
      destinationAddress: 'Rua B',
    });

    component.submitCreate();

    expect(rideService.createRide).toHaveBeenCalled();
    expect(rideService.listRides).toHaveBeenCalledTimes(1);
  });

  it('should update ride addresses with the selected client id', () => {
    const component = fixture.componentInstance as unknown as ClientComponentInternals;
    component.startEditingRide(rides[0]);
    component.editForm.setValue({
      startAddress: ' Avenida A ',
      destinationAddress: ' Rua B ',
    });

    component.saveEditingRide(rides[0]);

    expect(rideService.updateRide).toHaveBeenCalledWith('ride-1', {
      userId: 'client-1',
      startAddress: 'Avenida A',
      destinationAddress: 'Rua B',
    } satisfies UpdateRideRequestDto);
  });

  it('should not call the update API when edit form is invalid', () => {
    const component = fixture.componentInstance as unknown as ClientComponentInternals;
    component.startEditingRide(rides[0]);
    component.editForm.setValue({
      startAddress: '',
      destinationAddress: '',
    });

    component.saveEditingRide(rides[0]);

    expect(rideService.updateRide).not.toHaveBeenCalled();
  });

  it('should show inline edit fields after clicking the edit icon', async () => {
    await fixture.whenStable();
    fixture.detectChanges();

    const editIcon = (fixture.nativeElement as HTMLElement).querySelector<HTMLElement>('.pi-pencil');
    const editButton = editIcon?.closest('button');
    expect(editIcon).not.toBeNull();
    expect(editButton).not.toBeNull();

    editButton?.click();
    fixture.detectChanges();

    const startInput = (fixture.nativeElement as HTMLElement).querySelector<HTMLInputElement>(
      '#edit-start-ride-1'
    );
    const destinationInput = (fixture.nativeElement as HTMLElement).querySelector<HTMLInputElement>(
      '#edit-destination-ride-1'
    );

    expect(startInput).not.toBeNull();
    expect(destinationInput).not.toBeNull();
    expect(startInput?.value).toBe('Unifor');
    expect(destinationInput?.value).toBe('Praia de Iracema');
  });

  it('should only allow editing rides that are not finished', () => {
    const component = fixture.componentInstance as unknown as ClientComponentInternals;

    expect(component.canEditRide({ ...rides[0], status: 'CREATED' })).toBeTrue();
    expect(component.canEditRide({ ...rides[0], status: 'IN_PROGRESS' })).toBeTrue();
    expect(component.canEditRide({ ...rides[0], status: 'COMPLETED' })).toBeFalse();
    expect(component.canEditRide({ ...rides[0], status: 'CANCELLED' })).toBeFalse();
  });
});