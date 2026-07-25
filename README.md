# Crypto Detail

Android cryptocurrency tracker built with Kotlin and Jetpack Compose.

## Local setup

Add the following line to your `local.properties` file:

```properties
API_KEY=replace-with-Coingecko-demo-key
BASE_URL=replace-with-latest-coinGecko-base-url
```
Do not commit `local.properties` or API keys.

---

## Features

- Searchable cryptocurrency list with pull-to-refresh.
- Coin details, market metrics, and interactive INR price chart.
- 7-day and 30-day chart ranges.
- Loading, error, and empty states.

---

## Tech Stack

Kotlin, Jetpack Compose, MVVM, Hilt, Retrofit, Coil, Coroutines, Flow, and CoinGecko API.

## Known Limitation

The assignment did not specify an API provider or API documentation. This project uses CoinGecko Demo API, which has fixed quota and rate limits for the configured key. A paid plan or an alternative provider may be required after the free allowance if exhausted.

## Media Attachments

## Download APK

Download and install the latest APK to try the application.

<p align="center">

<a href="https://github.com/Nerd0Vidhan/CryptoDetail/blob/master/resources/app/CryptoDetail.apk">
    <img src="https://img.shields.io/badge/Download-APK-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Download APK"/>
</a>

</p>

Or click here: **[CryptoDetail.apk](https://github.com/Nerd0Vidhan/CryptoDetail/blob/master/resources/app/CryptoDetail.apk)**

## Screen Recording

App demo video: [Watch the recording](https://github.com/Nerd0Vidhan/CryptoDetail/blob/master/resources/video/app.mp4)

## App Screen Previews

| Crypto List | Coin Detail |
|---|---|
| <img src="https://github.com/Nerd0Vidhan/CryptoDetail/blob/master/resources/screenshot/crypto-list.png" width="220"/> | <img src="https://github.com/Nerd0Vidhan/CryptoDetail/blob/master/resources/screenshot/coin-detail.png" width="220"/> |

The app reads this value into `BuildConfig` and sends it as CoinGecko's `x-cg-demo-api-key` header and base url.

### Libraries

- Hilt: dependency injection.
- Retrofit, Gson, and OkHttp: CoinGecko HTTP client and JSON parsing.
- Coil: Compose image loading and caching.
- Navigation Compose: screen navigation.
- MockK, MockWebServer, coroutines-test: isolated asynchronous and network-contract tests.
