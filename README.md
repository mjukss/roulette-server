# Roulette

![Alt Text](https://media0.giphy.com/media/a8QPwlgUJoKuumPSFo/giphy.gif)

### [Link to demo](https://wheel-evo.netlify.com)

To start server, in the project directory, you can run:

### `sbt run`

### HTTP endpoints

register player

```console
curl -X POST http://localhost:8080/register
   -H 'Content-Type: application/json'
   -d '{"username":$username,"password":$password}'
```

remove player

```console
curl -X POST http://localhost:8080/remove
   -H 'Content-Type: application/json'
   -d '{"username":$username,"password":$password}'
```

### WS endpoints

open connection on ws://localhost:8080

join game

```json
  {
  "requestType": "JoinGame",
  "username": $username,
  "password": $password
  }
```

place bet

```json
  {
   "requestType": "PlaceBet",
    "bet": {
        "betType": "Straight",
        "betAmount": $betAmount,
        "positions": [ $position ]
    }
  }
```

clear bets

```json
  { "requestType": "ClearBets" }
```

exit game

```json
  { "requestType": "ExitGame" }
```

### Bet types

    Straight
    Odd
    Even
    High
    Low
    Row
    Dozen
    Red
    Black
    Split
    Street(
    SixLine
    Corner
    Trio
    Basket
