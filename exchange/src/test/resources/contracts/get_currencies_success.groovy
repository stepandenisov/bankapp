package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Получить все валюты"
    request {
        method GET()
        url "/rate/currencies"
    }
    response {
        status 200
        body(["USD", "CNY", "RUB"])
        headers {
            contentType(applicationJson())
        }
    }
}
