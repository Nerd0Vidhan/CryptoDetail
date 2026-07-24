# Crypto Detail

## Local setup

Add the following line to your `local.properties` file:

```properties
API_KEY=replace-with-Coingecko-demo-key
BASE_URL=replace-with-latest-coinGecko-base-url
```

The app reads this value into `BuildConfig` and sends it as CoinGecko's `x-cg-demo-api-key` header and base url.

### Libraries

- Hilt: dependency injection.
- Retrofit, Gson, and OkHttp: CoinGecko HTTP client and JSON parsing.
- Coil: Compose image loading and caching.
- Navigation Compose: screen navigation.
- MockK, MockWebServer, coroutines-test: isolated asynchronous and network-contract tests.
