package contracts.account

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Добавление нового аккаунта"

    request {
        method POST()
        url("/accounts")
        headers {
            contentType(applicationJson())
        }
        body([
                type    : "USD",
                balance : 0.0
        ])
    }

    response {
        status 200
    }
}
