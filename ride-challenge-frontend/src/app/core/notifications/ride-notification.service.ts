import { Injectable, OnDestroy } from '@angular/core';
import { IMessage, RxStomp } from '@stomp/rx-stomp';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import type { RideNotificationDto } from '../api/api-dtos';

const RIDES_TOPIC = '/topic/rides';

@Injectable({ providedIn: 'root' })
export class RideNotificationService implements OnDestroy {
  private readonly rxStomp = new RxStomp();

  constructor() {
    this.rxStomp.configure({
      brokerURL: environment.wsUrl,
      reconnectDelay: 5000,
      heartbeatIncoming: 0,
      heartbeatOutgoing: 20000,
    });
  }

  watchRides(): Observable<RideNotificationDto> {
    this.connect();
    return this.rxStomp
      .watch(RIDES_TOPIC)
      .pipe(map((message: IMessage) => JSON.parse(message.body) as RideNotificationDto));
  }

  connect(): void {
    if (!this.rxStomp.active) {
      this.rxStomp.activate();
    }
  }

  ngOnDestroy(): void {
    void this.rxStomp.deactivate();
  }
}
