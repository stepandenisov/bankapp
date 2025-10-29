package contracts.account

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Получение accountId по userId и currency"

    request {
        method GET()
        urlPath("/accounts/findAccountId") {
            queryParameters {
                parameter("userId", "1")
                parameter("currency", "RUB")
            }
        }
        headers {
            accept(applicationJson())
        }
    }

    response {
        status 200
        headers {
            contentType(applicationJson())
        }
        body(2L)
    }
}
