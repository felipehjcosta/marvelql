# MarvelQL
[![Build Status](https://travis-ci.org/felipehjcosta/marvelql.svg?branch=master)](https://travis-ci.org/felipehjcosta/marvelql)
A GraphQL API for [The Marvel Comics API](https://developer.marvel.com) made with [Kotlin](https://kotlinlang.org) and [Ktor](https://ktor.io). It's on early stage development.

[logo]: https://raw.githubusercontent.com/felipehjcosta/marvelql/master/logo.svg

Dependencies
-------
1. [Java](https://www.java.com)
2. [Docker](https://www.docker.com)
2. [Marvel API Keys](https://developer.marvel.com/account) - Need the secret and public keys.

How to run
-------
1. Clone the repo
2. Add the secret and public keys to your `~/.gradle/gradle.properties`  located in your home like this:
```
MARVEL_PRIVATE_KEY=YOUR_SECRET_KEY
MARVEL_PUBLIC_KEY=YOUR_PUBLIC_KEY
```
3. Start service
```
./gradlew run
```

How to run tests
-------
You can run the system, integration and unit tests with the following command:
```
./gradlew check
```

License
-------

MIT License

Copyright (c) 2018 Felipe Costa

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
