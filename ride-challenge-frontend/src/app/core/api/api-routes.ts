const accountsRoute = (baseUrl: string): string => `${baseUrl}/accounts`;
const authRoute = (baseUrl: string): string => `${baseUrl}/auth`;
const ridesRoute = (baseUrl: string): string => `${baseUrl}/rides`;
const rideByIdRoute = (baseUrl: string, rideId: string): string => `${ridesRoute(baseUrl)}/${rideId}`;

export const apiRoutes = {
  login: (baseUrl: string): string => `${authRoute(baseUrl)}/login`,
  accounts: accountsRoute,
  accountById: (baseUrl: string, accountId: string): string =>
    `${accountsRoute(baseUrl)}/${accountId}`,
  rides: ridesRoute,
  rideById: rideByIdRoute,
  updateRide: rideByIdRoute,
  rideStatus: (baseUrl: string, rideId: string): string =>
    `${rideByIdRoute(baseUrl, rideId)}/status`,
  acceptRide: (baseUrl: string, rideId: string): string =>
    `${rideByIdRoute(baseUrl, rideId)}/accept`,
  cancelRide: (baseUrl: string, rideId: string): string =>
    `${rideByIdRoute(baseUrl, rideId)}/cancel`,
} as const;
