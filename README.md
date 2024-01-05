# CryptoAPI

This project is a telegram bot maven application.
The bot makes api calls to retrieve real time data on cryptocurrencies. The bot name is `Cryptobot` and the bot username is `Neuneu_bot`.
Users can interact with the bot to get alerts on some cryptocurrency market conditions.

# Development

## Prerequisites

Install java runtime environment, java development kit and maven on your machine.

## Setting environment variables

Create a `.env` file in the root of your application using the `.env-sample` model.
Get your CoinMarketCap api key: `https://coinmarketcap.com/api/pricing/`.
Get your Telegram api key from the `BotFather` bot.
Get your Etherscan key: `https://etherscan.io/apis`.

Add your keys to the `.env` file.

## Compiling the application

In the root of the application run:
### `mvn clean compile assembly:single`

## Executing the application

In the root of the application run:
### `java -cp target/CryptoAPI-1.0-SNAPSHOT-jar-with-dependencies.jar cryptoapi.App`
