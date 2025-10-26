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
                        currency: "RUB",
                        reminder : 1000.00
                ]
        ])
    }
}
