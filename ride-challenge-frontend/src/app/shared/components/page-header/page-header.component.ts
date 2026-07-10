import { ChangeDetectionStrategy, Component, input } from '@angular/core';

@Component({
  selector: 'app-page-header',
  standalone: true,
  template: `
    <header class="page-header">
      <div>
        <h1>{{ title() }}</h1>
        @if (subtitle(); as subtitleText) {
          <p class="page-subtitle">{{ subtitleText }}</p>
        }
      </div>
      <ng-content />
    </header>
  `,
  styles: `
    .page-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 1rem;
      margin-bottom: 1.5rem;
    }

    .page-header h1 {
      margin: 0;
    }

    .page-subtitle {
      margin: 0.35rem 0 0;
      color: #616161;
      font-size: 0.875rem;
    }

    @media (max-width: 720px) {
      .page-header {
        align-items: stretch;
        flex-direction: column;
      }
    }
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PageHeaderComponent {
  readonly title = input.required<string>();
  readonly subtitle = input<string | null>();
}
