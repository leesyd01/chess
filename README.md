# ♕ BYU CS 240 Chess

This project demonstrates mastery of proper software design, client/server architecture, networking using HTTP and WebSocket, database persistence, unit testing, serialization, and security.

## 10k Architecture Overview

The application implements a multiplayer chess server and a command line chess client.

[![Sequence Diagram](10k-architecture.png)](https://sequencediagram.org/index.html#initialData=C4S2BsFMAIGEAtIGckCh0AcCGAnUBjEbAO2DnBElIEZVs8RCSzYKrgAmO3AorU6AGVIOAG4jUAEyzAsAIyxIYAERnzFkdKgrFIuaKlaUa0ALQA+ISPE4AXNABWAexDFoAcywBbTcLEizS1VZBSVbbVc9HGgnADNYiN19QzZSDkCrfztHFzdPH1Q-Gwzg9TDEqJj4iuSjdmoMopF7LywAaxgvJ3FC6wCLaFLQyHCdSriEseSm6NMBurT7AFcMaWAYOSdcSRTjTka+7NaO6C6emZK1YdHI-Qma6N6ss3nU4Gpl1ZkNrZwdhfeByy9hwyBA7mIT2KAyGGhuSWi9wuc0sAI49nyMG6ElQQA)

## Modules

The application has three modules.

- **Client**: The command line program used to play a game of chess over the network.
- **Server**: The command line program that listens for network requests from the client and manages users and games.
- **Shared**: Code that is used by both the client and the server. This includes the rules of chess and tracking the state of a game.

## Starter Code

As you create your chess application you will move through specific phases of development. This starts with implementing the moves of chess and finishes with sending game moves over the network between your client and server. You will start each phase by copying course provided [starter-code](starter-code/) for that phase into the source code of the project. Do not copy a phases' starter code before you are ready to begin work on that phase.

## IntelliJ Support

Open the project directory in IntelliJ in order to develop, run, and debug your code using an IDE.

## Maven Support

You can use the following commands to build, test, package, and run your code.

| Command                    | Description                                     |
| -------------------------- | ----------------------------------------------- |
| `mvn compile`              | Builds the code                                 |
| `mvn package`              | Run the tests and build an Uber jar file        |
| `mvn package -DskipTests`  | Build an Uber jar file                          |
| `mvn install`              | Installs the packages into the local repository |
| `mvn test`                 | Run all the tests                               |
| `mvn -pl shared test`      | Run all the shared tests                        |
| `mvn -pl client exec:java` | Build and run the client `Main`                 |
| `mvn -pl server exec:java` | Build and run the server `Main`                 |

These commands are configured by the `pom.xml` (Project Object Model) files. There is a POM file in the root of the project, and one in each of the modules. The root POM defines any global dependencies and references the module POM files.

## Running the program using Java

Once you have compiled your project into an uber jar, you can execute it with the following command.

```sh
java -jar client/target/client-jar-with-dependencies.jar

♕ 240 Chess Client: chess.ChessPiece@7852e922
```

#### PHASE 2 UML SEQUENCE DIAGRAM LINK
https://sequencediagram.org/index.html?presentationMode=readOnly#initialData=IYYwLg9gTgBAwgGwJYFMB2YBQAHYUxIhK4YwDKKUAbpTngUSWDABLBoAmCtu+hx7ZhWqEUdPo0EwAIsDDAAgiBAoAzqswc5wAEbBVKGBx2ZM6MFACeq3ETQBzGAAYAdAE5M9qBACu2AMQALADMABwATG4gMP7I9gAWYDoIPoYASij2SKoWckgQaJiIqKQAtAB85JQ0UABcMADaAAoA8mQAKgC6MAD0PgZQADpoAN4ARP2UaMAAtihjtWMwYwA0y7jqAO7QHAtLq8soM8BICHvLAL6YwjUwFazsXJT145NQ03PnB2MbqttQu0WyzWYyOJzOQLGVzYnG4sHuN1E9SgmWyYEoAAoMlkcpQMgBHVI5ACU12qojulVk8iUKnU9XsKDAAFUBhi3h8UKTqYplGpVJSjDpagAxJCcGCsyg8mA6SwwDmzMQ6FHAADWkoGME2SDA8QVA05MGACFVHHlKAAHmiNDzafy7gjySp6lKoDyySIVI7KjdnjAFKaUMBze11egAKKWlTYAgFT23Ur3YrmeqBJzBYbjObqYCMhbLCNQbx1A1TJXGoMh+XyNXoKFmTiYO189Q+qpelD1NA+BAIBMU+4tumqWogVXot3sgY87nae1t+7GWoKDgcTXS7QD71D+et0fj4PohQ+PUY4Cn+Kz5t7keC5er9cnvUexE7+4wp6l7FovFqXtYJ+cLtn6pavIaSpLPU+wgheertBAdZoFByyXAmlDtimGD1OEThOFmEwQZ8MDQcCyxwfECFISh+xXOgHCmF4vgBNA7CMjEIpwBG0hwAoMAADIQFkhRYcwTrUP6zRtF0vQGOo+RoARiqfJCIK-P8gK0eh8KVEB-rgeWKkwes+h-DsXzQo8wHiVQSIwAgQnihignCQSRJgKSb6GBUw78gyTJTspXI3jS+73sKYoSm6MpymW7xKpgKrBhqbowGgEDMAAZr4ErQNq3gODA6k7CFvJ3kmvrOl2aW9ggMB5dFW5eSB1T+sy0yXtASAAF4oBwUYxnGhSgZhyCpjA6YAIwETmqh5vM0FFiW9Q+B1epdb1ux0U2vmLjZdmNfI27eeUu0HhOKDPvE56XteZ3hSua4BrdTVVe2emli54oZKoAGYB9LUSWBhGGfMJGod8FFUfW4NaSNFXwGN2EwLh+GjCD8XEaRkOXtDyGw2h9GMd4fj+F4KDoDEcSJOTlMub4WCiYKoH1A00gRvxEbtBG3Q9HJqgKcMFEbXkBT1AAPFDiHoOU2mCh99RS0h-1WRh+0ujIt5+TAjJgFdN3wdLaCkkl6rPXqMCQEhaUZTA2WrQx90I153a1cdgM1K6a3xBtfUDSgsYKXLCOiWmTjTejs3zQWYxLdAK3e77W2No7Wt7ZVnb1Fdr5vbuoUjrUHAoNwx6XgblFG3O+cOkuwoZDMEA0ObV6vZ272q59QkMz9f0A87rVgVc8PJkjYA4XhWYXDAKfE8x-gouu-jYOKGr8WiMAAOJKhoTM2ZJG9c7z9hKkLnVQD1otoBLSsy8Hukd4ruNGyrsJqxntka8gORbzmzloj-ag3JqA8u7POZVta631jfY2pUFwClrqKcUT4XryFlPKaBiVVQaiupbI2Nsso5VTtXdOHYP7VR7H2UBe9SztWFufTa-tA7xmHpUUOE1w4zX5NHRaxZ44KkTvQvqDYiZO3VtVbOrcyGCjOrUL+YAAGqAxFXcBi5KjLkiuuE0dUAEelEe-Oya8cjix0doWWzUEYKwEv-bePcECAQ7h7fSYxj45gLA0cYLiUAAElpALEmuEYIgQQSbHiLqFAbpORfBBMkUAaoImQUwKpZYniABykFIQXE6EPAeo0ShjxRhPdGnjVBuJcGUrJM9PAkwCBwAA7G4JwKAnAxAjMEOAXEABs8ALqbwrEUUezMB6s1aB0I+J8BEXyGtfJ+SFygEVSekuGOSPwP2NDM9AawPFKjSUZNCfd9Ea0PHIFAACMRwAugAoBJJQFUjTqOHWTIoHrJgU7NREUkHNxiug55mDkrN1wdbdKBCHawLCv3TONVKHmOoV7OhPU-bRgDkNYOI88lhwjtmLh+YeHLX4XCzawidp3I9nZCRR1zG3OIedI8JylQYk8XdYlCDHy9LmLoplYiGRKh8VQ++r9SznJpZc-8diX5fkccDTxPiCz+MCdkoGuTxqo3mdy3x9RZWBG2gxKpc9LDFwcpsKmSAEhgD1X2CAhqABSEBxSssMP4GJIA1T9LyYMoGwzmQyR6J40+616GX2mYbWZBFsAIGAHqqAcAIAOSgHsAA6iwLx3MegACF+IKDgAAaW+FKtVGr5WJj5V+R+QaNkwHGKG8NlAo0xqick1VfiAmarFdZA51UABWNq0CnOteKYVhJgGeVzpSlR9zIFl2gcouBD0NGfO0GgtZpbCim2wZeAFlMgV20IaC8qnLIX9mhe-NqEyGGIqYcNZZqLxpTU4bmbFhZeGllWvioRWqd01z3WS4ANzNZUtqLrU5uap1greYgiUJjUGxVzb8s2AD8FbodvVWAeh1wogHTkd9JCXb7t5aQ-0KaQxXLAIw5FLDEZovYWjTFd6FoPtxShmAaH3KEqIaOklGsIPfpXTAIjfUYAgGjXlE0ZoazhgvW3cFnsAxVlDGJkjQcyNsPTJmSOWLaOx0fYrGTonqJvopb+0dtQ-BaHRIBpUjKqUPWZNgEzhhOPal1PqNAKBDWVojbhyxvbu1KlsfY-lEqXgFrVle5GyrRhvp1aTLw4ajUmui-KRAwZYDAGwKGwgl9p67yPaWNmHMuY816MYO+Dx+X1AxOlWUEBzSkn2aQuyIBuB4CUT+mRDWkvNdeUKWo0hi5MkMFojc7otydeXD1ku-W+xzqOiN4UY2+uVm0eZyRg4VmlcRkl3zLbgvZcCyi1ho9x5UbfUAA