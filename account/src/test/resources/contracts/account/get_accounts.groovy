package contracts.account

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Получение списка аккаунтов текущего пользователя"

    request {
        method GET()
        url("/accounts")
        headers {
            accept(applicationJson())
        }
    }

    response {
        status 200
        headers {
            contentType(applicationJson())
        }
        body([
                [
                        id      : 1L,
                        currency: "USD",
                        reminder: 900.0
                ],
                [
                        id      : 2L,
                        currency: "RUB",
                        reminder: 10000.0
                ],
                [
                        id      : 3L,
                        currency: "CNY",
                        reminder: 0.0
                ]
        ])
    }
}
