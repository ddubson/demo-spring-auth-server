# 🏰 Spring Authorization Server Demo

[![Demo Spring Auth Server build](https://github.com/ddubson/demo-spring-auth-server/actions/workflows/gradle.yml/badge.svg)](https://github.com/ddubson/demo-spring-auth-server/actions/workflows/gradle.yml)

A live example of an implementation of Spring Authorization Server, based on https://github.com/spring-projects/spring-authorization-server

> **Warning**
> This project does not use secure configuration methods -- only demonstrates features. Please use caution when referencing material contained in this repository.

## ⭐️ Features

- ✅ OpenID Connect Dynamic Client Registration 1.0
  - Use file `./dynamic-client-registration.http` as an example walkthrough

## 🚚 Operation

To run locally,

```shell
./gradlew bootRun
```

> Available at `127.0.0.1:9000`
