import { Injectable, OnDestroy, inject } from '@angular/core';
import { IMessage, RxStomp } from '@stomp/rx-stomp';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import type { RideNotificationDto } from '../api/api-dtos';
import { SessionService } from '../session/session.service';

const RIDES_TOPIC = '/topic/rides';

@Injectable({ providedIn: 'root' })
export class RideNotificationService implements OnDestroy {
  private readonly session = inject(SessionService);
  private readonly rxStomp = new RxStomp();
  private brokerURL: string | null = null;

  constructor() {
    this.rxStomp.configure({
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
    const token = this.session.token();
    if (!token) return;

    const brokerURL = `${environment.wsUrl}?access_token=${encodeURIComponent(token)}`;
    if (this.brokerURL !== brokerURL) {
      this.rxStomp.configure({ brokerURL });
      this.brokerURL = brokerURL;
    }

    if (!this.rxStomp.active) {
      this.rxStomp.activate();
    }
  }

  ngOnDestroy(): void {
    void this.rxStomp.deactivate();
  }
}
